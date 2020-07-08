package us.myles.ViaVersion.api.minecraft;

public class BlockChangeRecord1_8 implements BlockChangeRecord {
    private final byte sectionX;
    private final short y;
    private final byte sectionZ;
    private int blockId;

    public BlockChangeRecord1_8(byte sectionX, short y, byte sectionZ, int blockId) {
        this.sectionX = sectionX;
        this.y = y;
        this.sectionZ = sectionZ;
        this.blockId = blockId;
    }

    public BlockChangeRecord1_8(int sectionX, int y, int sectionZ, int blockId) {
        this((byte) sectionX, (short) y, (byte) sectionZ, blockId);
    }

    /**
     * @return x coordinate within the chunk section
     */
    public byte getSectionX() {
        return sectionX;
    }

    @Override
    public byte getSectionY() {
        return (byte) (y & 0xF);
    }

    /**
     * @return y coordinate
     */
    @Override
    public short getY(int chunkSectionY) {
        return y;
    }

    /**
     * @return z coordinate within the chunk section
     */
    public byte getSectionZ() {
        return sectionZ;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
