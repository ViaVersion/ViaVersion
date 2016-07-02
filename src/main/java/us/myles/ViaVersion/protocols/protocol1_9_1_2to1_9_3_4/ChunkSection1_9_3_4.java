package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;

public class ChunkSection1_9_3_4 implements ChunkSection {
    private final byte[] data;

    public ChunkSection1_9_3_4(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        throw new UnsupportedOperationException("Invalid chunk type!");
    }

    @Override
    public void writeBlocks(ByteBuf output) throws Exception {
        throw new UnsupportedOperationException("Invalid chunk type!");
    }

    @Override
    public void writeBlockLight(ByteBuf output) throws Exception {
        throw new UnsupportedOperationException("Invalid chunk type!");
    }

    @Override
    public boolean hasSkyLight() {
        throw new UnsupportedOperationException("Invalid chunk type!");
    }

    @Override
    public void writeSkyLight(ByteBuf output) throws Exception {
        throw new UnsupportedOperationException("Invalid chunk type!");
    }
}
