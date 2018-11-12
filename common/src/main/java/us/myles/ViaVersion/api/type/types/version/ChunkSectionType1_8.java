package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class ChunkSectionType1_8 extends Type<ChunkSection> {

    public ChunkSectionType1_8() {
        super("Chunk Section Type", ChunkSection.class);
    }

    @Override
    public ChunkSection read(ByteBuf buffer) throws Exception {
        ChunkSection chunkSection = new ChunkSection();
        chunkSection.clearPalette();

        byte[] blockData = new byte[ChunkSection.SIZE * 2];
        buffer.readBytes(blockData);
        ShortBuffer blockBuf = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

        for (int i = 0; i < ChunkSection.SIZE; i++) {
            int mask = blockBuf.get();
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
