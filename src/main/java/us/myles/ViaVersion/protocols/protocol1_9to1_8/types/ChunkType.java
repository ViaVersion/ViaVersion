package us.myles.ViaVersion.protocols.protocol1_9to1_8.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.BitSet;
import java.util.logging.Level;

public class ChunkType extends PartialType<Chunk, ClientChunks> {
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

    public ChunkType(ClientChunks chunks) {
        super(chunks, Chunk.class);
    }

    private static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - -2147483648L;
    }

    @Override
    public Chunk read(ByteBuf input, ClientChunks param) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        long chunkHash = toLong(chunkX, chunkZ);
        boolean groundUp = input.readByte() != 0;
        int bitmask = input.readUnsignedShort();
        int dataLength = Type.VAR_INT.read(input);

        // Data to be read
        BitSet usedSections = new BitSet(16);
        ChunkSection[] sections = new ChunkSection[16];
        byte[] biomeData = null;

        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }
        int sectionCount = usedSections.cardinality(); // the amount of sections set

        // If the chunk is from a chunk bulk, it is never an unload packet
        // Other wise, if it has no data, it is :)
        boolean isBulkPacket = param.getBulkChunks().remove(chunkHash);
        if (sectionCount == 0 && groundUp && !isBulkPacket && param.getLoadedChunks().contains(chunkHash)) {
            // This is a chunk unload packet
            param.getLoadedChunks().remove(chunkHash);
            return new Chunk(chunkX, chunkZ);
        }

        int startIndex = input.readerIndex();
        param.getLoadedChunks().add(chunkHash); // mark chunk as loaded

        // Read blocks
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection section = new ChunkSection();
            sections[i] = section;

            // Read block data and convert to short buffer
            byte[] blockData = new byte[ChunkSection.SIZE * 2];
            input.readBytes(blockData);
            ShortBuffer blockBuf = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            for (int j = 0; j < ChunkSection.SIZE; j++) {
                int mask = blockBuf.get();
                int type = mask >> 4;
                int data = mask & 0xF;
                section.setBlock(j, type, data);
            }
        }

        // Read block light
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set, has no light
            byte[] blockLightArray = new byte[ChunkSection.LIGHT_LENGTH];
            input.readBytes(blockLightArray);
            sections[i].setBlockLight(blockLightArray);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if (bytesLeft >= ChunkSection.LIGHT_LENGTH) {
            for (int i = 0; i < SECTION_COUNT; i++) {
                if (!usedSections.get(i)) continue; // Section not set, has no light
                byte[] skyLightArray = new byte[ChunkSection.LIGHT_LENGTH];
                input.readBytes(skyLightArray);
                sections[i].setSkyLight(skyLightArray);
                bytesLeft -= ChunkSection.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if (bytesLeft >= BIOME_DATA_LENGTH) {
            biomeData = new byte[BIOME_DATA_LENGTH];
            input.readBytes(biomeData);
            bytesLeft -= BIOME_DATA_LENGTH;
        }

        // Check remaining bytes
        if (bytesLeft > 0) {
            Bukkit.getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunk! (" + groundUp + ")");
        }

        // Return chunk
        return new Chunk(chunkX, chunkZ, groundUp, bitmask, sections, biomeData);
    }

    @Override
    public void write(ByteBuf output, ClientChunks param, Chunk chunk) throws Exception {
        if (chunk.isUnloadPacket()) return;

        // Write primary info
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        output.writeByte(chunk.isGroundUp() ? 0x01 : 0x00);
        Type.VAR_INT.write(output, chunk.getPrimaryBitmask());

        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < SECTION_COUNT; i++) {
            ChunkSection section = chunk.getSections()[i];
            if (section == null) continue; // Section not set
            section.writeBlocks(buf);
            section.writeBlockLight(buf);
            if (!section.hasSkyLight()) continue; // No sky light, we're done here.
            section.writeSkyLight(buf);
        }
        buf.readerIndex(0);
        Type.VAR_INT.write(output, buf.readableBytes() + (chunk.hasBiomeData() ? 256 : 0));
        output.writeBytes(buf);
        buf.release(); // release buffer

        // Write biome data
        if (chunk.hasBiomeData()) {
            output.writeBytes(chunk.getBiomeData());
        }
    }
}
