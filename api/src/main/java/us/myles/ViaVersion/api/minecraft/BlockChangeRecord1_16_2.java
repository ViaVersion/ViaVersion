package us.myles.ViaVersion.api.minecraft;

import com.google.common.base.Preconditions;

public class BlockChangeRecord1_16_2 implements BlockChangeRecord {
    private final byte sectionX;
    private final byte sectionY;
    private final byte sectionZ;
    private int blockId;

    public BlockChangeRecord1_16_2(byte sectionX, byte sectionY, byte sectionZ, int blockId) {
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;
        this.blockId = blockId;
    }

    public BlockChangeRecord1_16_2(int sectionX, int sectionY, int sectionZ, int blockId) {
        this((byte) sectionX, (byte) sectionY, (byte) sectionZ, blockId);
    }

    @Override
    public byte getSectionX() {
        return sectionX;
    }

    @Override
    public byte getSectionY() {
        return sectionY;
    }

    @Override
    public byte getSectionZ() {
        return sectionZ;
    }

    @Override
    public short getY(int chunkSectionY) {
        Preconditions.checkArgument(chunkSectionY >= 0 && chunkSectionY < 16, "Invalid chunkSectionY: " + chunkSectionY);
        return (short) ((chunkSectionY << 4) + sectionY);
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
