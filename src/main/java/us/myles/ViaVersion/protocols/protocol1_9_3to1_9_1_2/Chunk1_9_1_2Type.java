package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import io.netty.buffer.ByteBuf;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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

//        byte[] sections = new byte[size];
//        input.readBytes(sections);

        BitSet usedSections = new BitSet(16);
        ChunkSection1_9_1_2[] sections = new ChunkSection1_9_1_2[16];
        byte[] biomeData = null;

        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((primaryBitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }
        int sectionCount = usedSections.cardinality(); // the amount of sections set
        // Read sections
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection1_9_1_2 section = new ChunkSection1_9_1_2();
            sections[i] = section;

            short bitsPerBlock = input.readUnsignedByte();
            Integer[] palette = Type.VAR_INT_ARRAY.read(input); // 0 if none
            // Read blocks
            Long[] data = Type.LONG_ARRAY.read(input);

        }

        int blockEntities = Type.VAR_INT.read(input);
        List<CompoundTag> nbtData = new ArrayList<>();
        for (int i = 0; i < blockEntities; i++) {
            nbtData.add(Type.NBT.read(input));
        }
        return new Chunk1_9_1_2(chunkX, chunkZ, groundUp, primaryBitmask, sections, new byte[0], nbtData);
    }

    @Override
    public void write(ByteBuf buffer, Chunk input) throws Exception {
        if (!(input instanceof Chunk1_9_1_2))
            throw new Exception("Tried to send the wrong chunk type from 1.9.3-4 chunk: " + input.getClass());
        Chunk1_9_1_2 chunk = (Chunk1_9_1_2) input;

        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());

        buffer.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(buffer, chunk.getBitmask());

        Type.VAR_INT.write(buffer, chunk.getSections().length);
//        buffer.writeBytes(chunk.getSections());

        // no block entities as it's earlier
    }
}
