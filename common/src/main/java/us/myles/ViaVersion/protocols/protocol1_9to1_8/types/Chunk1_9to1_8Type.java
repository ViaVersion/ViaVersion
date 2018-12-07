package us.myles.ViaVersion.protocols.protocol1_9to1_8.types;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

public class Chunk1_9to1_8Type extends PartialType<Chunk, ClientChunks> {
    /**
     * Amount of sections in a chunks.
     */
    public static final int SECTION_COUNT = 16;
    /**
     * size of each chunks section (16x16x16).
     */
    private static final int SECTION_SIZE = 16;
    /**
     * Length of biome data.
     */
    private static final int BIOME_DATA_LENGTH = 256;

    public Chunk1_9to1_8Type(ClientChunks chunks) {
        super(chunks, Chunk.class);
    }

    private static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - -2147483648L;
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }

    @Override
    public Chunk read(ByteBuf input, ClientChunks param) throws Exception {
        boolean replacePistons = param.getUser().get(ProtocolInfo.class).getPipeline().contains(Protocol1_10To1_9_3_4.class) && Via.getConfig().isReplacePistons();
        int replacementId = Via.getConfig().getPistonReplacementId();

        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        long chunkHash = toLong(chunkX, chunkZ);
        boolean groundUp = input.readByte() != 0;
        int bitmask = input.readUnsignedShort();
        int dataLength = Type.VAR_INT.read(input);

        // Data to be read
        BitSet usedSections = new BitSet(16);
        ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = null;

        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }
        int sectionCount = usedSections.cardinality(); // the amount of sections set

        // If the chunks is from a chunks bulk, it is never an unload packet
        // Other wise, if it has no data, it is :)
        boolean isBulkPacket = param.getBulkChunks().remove(chunkHash);
        if (sectionCount == 0 && groundUp && !isBulkPacket && param.getLoadedChunks().contains(chunkHash)) {
            // This is a chunks unload packet
            param.getLoadedChunks().remove(chunkHash);
            return new Chunk1_8(chunkX, chunkZ);
        }

        int startIndex = input.readerIndex();
        param.getLoadedChunks().add(chunkHash); // mark chunks as loaded

        // Read blocks
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection section = Types1_8.CHUNK_SECTION.read(input);
            sections[i] = section;

            if (replacePistons) {
                section.replacePaletteEntry(36, replacementId);
            }
        }

        // Read block light
        for (int i = 0; i < SECTION_COUNT; i++) {
            if (!usedSections.get(i)) continue; // Section not set, has no light
            sections[i].readBlockLight(input);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if (bytesLeft >= ChunkSection.LIGHT_LENGTH) {
            for (int i = 0; i < SECTION_COUNT; i++) {
                if (!usedSections.get(i)) continue; // Section not set, has no light
                sections[i].readSkyLight(input);
                bytesLeft -= ChunkSection.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if (bytesLeft >= BIOME_DATA_LENGTH) {
            biomeData = new int[BIOME_DATA_LENGTH];
            for (int i = 0; i < BIOME_DATA_LENGTH; i++) {
                biomeData[i] = input.readByte() & 0xFF;
            }
            bytesLeft -= BIOME_DATA_LENGTH;
        }

        // Check remaining bytes
        if (bytesLeft > 0) {
            Via.getPlatform().getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunks! (" + groundUp + ")");
        }

        // Return chunks
        return new Chunk1_8(chunkX, chunkZ, groundUp, bitmask, sections, biomeData, new ArrayList<CompoundTag>());
    }

    @Override
    public void write(ByteBuf output, ClientChunks param, Chunk input) throws Exception {
        if (!(input instanceof Chunk1_8)) throw new Exception("Incompatible chunk, " + input.getClass());

        Chunk1_8 chunk = (Chunk1_8) input;
        // Write primary info
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        if (chunk.isUnloadPacket()) return;
        output.writeByte(chunk.isGroundUp() ? 0x01 : 0x00);
        Type.VAR_INT.write(output, chunk.getBitmask());

        ByteBuf buf = output.alloc().buffer();
        for (int i = 0; i < SECTION_COUNT; i++) {
            ChunkSection section = chunk.getSections()[i];
            if (section == null) continue; // Section not set
            Types1_9.CHUNK_SECTION.write(buf, section);
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
            for (int biome : chunk.getBiomeData()) {
                output.writeByte((byte) biome);
            }
        }
    }
}
