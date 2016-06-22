package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks.ChunkSection1_9to1_8;

import java.util.ArrayList;
import java.util.BitSet;

public class Chunk1_9_1_2Type extends BaseChunkType {
    public Chunk1_9_1_2Type() {
        super("1.9.1/2 Chunk");
    }

    @Override
    public Chunk read(ByteBuf input) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.read(input);
        int size = Type.VAR_INT.read(input);

        BitSet usedSections = new BitSet(16);
        ChunkSection1_9_1_2[] sections = new ChunkSection1_9_1_2[16];
        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((primaryBitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }

        // Read sections
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection1_9_1_2 section = new ChunkSection1_9_1_2();
            sections[i] = section;
            // WIP.
//            section.readBlocks(input);
//            section.readBlockLight(input);
//            section.readSkyLight(input);
            /*
                    BlockStorage blocks = new BlockStorage(in);
                    NibbleArray3d blocklight = new NibbleArray3d(in, 2048); (1024 bytes)
                    NibbleArray3d skylight = hasSkylight ? new NibbleArray3d(in, 2048) : null; (1024 bytes)
                    chunks[index] = new Chunk(blocks, blocklight, skylight);
             */
        }
        byte[] biomeData = groundUp ? new byte[256] : null;
        if (groundUp)
            input.readBytes(biomeData);

        return new Chunk1_9_1_2(chunkX, chunkZ, groundUp, primaryBitmask, sections, biomeData, new ArrayList<CompoundTag>());
    }

    @Override
    public void write(ByteBuf output, Chunk input) throws Exception {
        if (!(input instanceof Chunk1_9_1_2))
            throw new Exception("Tried to send the wrong chunk type from 1.9.3-4 chunk: " + input.getClass());
        Chunk1_9_1_2 chunk = (Chunk1_9_1_2) input;

        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        output.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(output, chunk.getBitmask());

        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 16; i++) {
            ChunkSection1_9_1_2 section = chunk.getSections()[i];
            if (section == null) continue; // Section not set
            section.writeBlocks(buf);
            section.writeBlockLight(buf);

            if (!section.hasSkyLight()) continue; // No sky light, we're done here.
            section.writeSkyLight(buf);

        }
        buf.readerIndex(0);
        Type.VAR_INT.write(output, buf.readableBytes() + (chunk.isBiomeData() ? 256 : 0));
        output.writeBytes(buf);
        buf.release(); // release buffer

        // Write biome data
        if (chunk.isBiomeData()) {
            output.writeBytes(chunk.getBiomeData());
        }

//        Type.NBT_ARRAY.write(output, tags.toArray(new CompoundTag[0])); Written by the handler
    }
}
