package us.myles.ViaVersion.api.minecraft;

public interface BlockChangeRecord {

    /**
     * @return relative x coordinate within the chunk section
     */
    byte getSectionX();

    /**
     * @return relative y coordinate within the chunk section
     */
    byte getSectionY();

    /**
     * @return relative z coordinate within the chunk section
     */
    byte getSectionZ();

    /**
     * @param chunkSectionY chunk section
     * @return absolute y coordinate
     */
    short getY(int chunkSectionY);

    /**
     * @return absolute y coordinate
     * @deprecated 1.16+ stores the relative y coordinate
     */
    @Deprecated
    default short getY() {
        return getY(-1);
    }

    int getBlockId();

    void setBlockId(int blockId);
}