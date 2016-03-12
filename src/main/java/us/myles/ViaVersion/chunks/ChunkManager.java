package us.myles.ViaVersion.chunks;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.BitSet;
import java.util.Set;
import java.util.logging.Level;

public class ChunkManager {
    /**
     * Amount of sections in a chunk.
     */
    private static final int SECTION_COUNT = 16;
    /**
     * size of each chunk section (16x16x16).
     */
    private static final int SECTION_SIZE = 16;
    /**
     * Length of biome data.
     */
    private static final int BIOME_DATA_LENGTH = 256;

    private final ConnectionInfo info;
    private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();
    private Method getWorldHandle;
    private Method getChunkAt;
    private Field getSections;

    public ChunkManager(ConnectionInfo info) {
        this.info = info;

        try {
            this.getWorldHandle = ReflectionUtil.obc("CraftWorld").getDeclaredMethod("getHandle");
            this.getChunkAt = ReflectionUtil.nms("World").getDeclaredMethod("getChunkAt", int.class, int.class);
            this.getSections = ReflectionUtil.nms("Chunk").getDeclaredField("sections");
            getSections.setAccessible(true);
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to initialise chunk verification", e);
        }
    }

    /**
     * Read chunk from 1.8 chunk data.
     *
     * @param input data
     * @return Chunk
     */
    public Chunk readChunk(ByteBuf input) {
        // Primary data
        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        long chunkHash = toLong(chunkX, chunkZ);
        boolean groundUp = input.readByte() != 0;
        int bitmask = input.readUnsignedShort();
        int dataLength = PacketUtil.readVarInt(input);

        // Data to be read
        BitSet usedSections = new BitSet(16);
        ChunkSection[] sections = new ChunkSection[16];
        byte[] biomeData = null;

        // Calculate section count from bitmask
        for(int i = 0; i < 16; i++) {
            if((bitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }

        // Unloading & empty chunks
        int sectionCount = usedSections.cardinality(); // the amount of sections set
        if(sectionCount == 0 && groundUp) {
            if(loadedChunks.contains(chunkHash)) {
                // This is a chunk unload packet
                loadedChunks.remove(chunkHash);
                return new Chunk(chunkX, chunkZ);
            } else {
                // Check if chunk data is invalid
                try {
                    Object nmsWorld = getWorldHandle.invoke(info.getPlayer().getWorld());
                    Object nmsChunk = getChunkAt.invoke(info.getPlayer().getWorld());
                    Object[] nmsSections = (Object[]) getSections.get(nmsChunk);

                    // Check if chunk is actually empty
                    boolean isEmpty = false;
                    int i = 0;
                    while(i < nmsSections.length) {
                        if(!(isEmpty = nmsSections[i++] == null)) break;
                    }
                    if(isEmpty) {
                        // not empty, LOL
                        return null;
                    }
                } catch(Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to verify chunk", e);
                }
            }
        }

        int startIndex = input.readerIndex();
        loadedChunks.add(chunkHash); // mark chunk as loaded

        // Read blocks
        for(int i = 0; i < SECTION_COUNT; i++) {
            if(!usedSections.get(i)) continue; // Section not set
            ChunkSection section = new ChunkSection();
            sections[i] = section;

            // Read block data and convert to short buffer
            byte[] blockData = new byte[ChunkSection.SIZE * 2];
            input.readBytes(blockData);
            ShortBuffer blockBuf = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            for(int j = 0; j < ChunkSection.SIZE; j++) {
                int mask = blockBuf.get();
                int type = mask >> 4;
                int data = mask & 0xF;
                section.setBlock(j, type, data);
            }
        }

        // Read block light
        for(int i = 0; i < SECTION_COUNT; i++) {
            if(!usedSections.get(i)) continue; // Section not set, has no light
            byte[] blockLightArray = new byte[ChunkSection.LIGHT_LENGTH];
            input.readBytes(blockLightArray);
            sections[i].setBlockLight(blockLightArray);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if(bytesLeft >= ChunkSection.LIGHT_LENGTH) {
            for(int i = 0; i < SECTION_COUNT; i++) {
                if(!usedSections.get(i)) continue; // Section not set, has no light
                byte[] skyLightArray = new byte[ChunkSection.LIGHT_LENGTH];
                input.readBytes(skyLightArray);
                sections[i].setSkyLight(skyLightArray);
                bytesLeft -= ChunkSection.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if(bytesLeft >= BIOME_DATA_LENGTH) {
            biomeData = new byte[BIOME_DATA_LENGTH];
            input.readBytes(biomeData);
            bytesLeft -= BIOME_DATA_LENGTH;
        }

        // Check remaining bytes
        if(bytesLeft > 0) {
            Bukkit.getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunk! (" + groundUp + ")");
        }

        // Return chunk
        return new Chunk(chunkX, chunkZ, groundUp, bitmask, sections, biomeData);
    }

    /**
     * Write chunk over 1.9 protocol.
     *
     * @param chunk chunk
     * @param output output
     */
    public void writeChunk(Chunk chunk, ByteBuf output) {
        if(chunk.isUnloadPacket()) {
            output.clear();
            PacketUtil.writeVarInt(0x1D, output);
        }

        // Write primary info
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        if(chunk.isUnloadPacket()) return;
        output.writeByte(chunk.isGroundUp() ? 0x01 : 0x00);
        PacketUtil.writeVarInt(chunk.getPrimaryBitmask(), output);

        ByteBuf buf = Unpooled.buffer();
        for(int i = 0; i < SECTION_COUNT; i++) {
            ChunkSection section = chunk.getSections()[i];
            if(section == null) continue; // Section not set
            section.writeBlocks(buf);
            section.writeBlockLight(buf);
            if(!section.hasSkyLight()) continue; // No sky light, we're done here.
            section.writeSkyLight(buf);
        }
        buf.readerIndex(0);
        PacketUtil.writeVarInt(buf.readableBytes() + (chunk.hasBiomeData() ? 256 : 0), output);
        output.writeBytes(buf);
        buf.release(); // release buffer

        // Write biome data
        if(chunk.hasBiomeData()) {
            output.writeBytes(chunk.getBiomeData());
        }
    }

    private static long toLong(int msw, int lsw)  {
        return ((long) msw << 32) + lsw - -2147483648L;
    }
}
