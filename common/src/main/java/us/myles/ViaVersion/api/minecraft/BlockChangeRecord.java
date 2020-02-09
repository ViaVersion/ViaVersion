package us.myles.ViaVersion.api.minecraft;

public class BlockChangeRecord {
    private final short horizontal;
    private final short y;
    private int blockId;

    public BlockChangeRecord(short horizontal, short y, int blockId) {
        this.horizontal = horizontal;
        this.y = y;
        this.blockId = blockId;
    }

    public short getHorizontal() {
        return horizontal;
    }

    public short getY() {
        return y;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
