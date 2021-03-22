package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;

import java.nio.ByteOrder;

public class ChunkSectionType1_8 extends Type<ChunkSection> {

    public ChunkSectionType1_8() {
        super("Chunk Section Type", ChunkSection.class);
    }

    @Override
    public ChunkSection read(ByteBuf buffer) throws Exception {
        ChunkSection chunkSection = new ChunkSection();
        // 0 index needs to be air in 1.9
        chunkSection.addPaletteEntry(0);

        ByteBuf littleEndianView = buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < ChunkSection.SIZE; i++) {
            int mask = littleEndianView.readShort();
            int type = mask >> 4;
            int data = mask & 0xF;
            chunkSection.setBlock(i, type, data);
        }

        return chunkSection;
    }

    @Override
    public void write(ByteBuf buffer, ChunkSection chunkSection) throws Exception {
        throw new UnsupportedOperationException();
    }
}
