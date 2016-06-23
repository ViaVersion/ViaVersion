package us.myles.ViaVersion.api.minecraft.chunks;

public interface Chunk {
    int getX();

    int getZ();

    ChunkSection[] getSections();

    boolean isGroundUp();

    boolean isBiomeData();

    byte[] getBiomeData();

    int getBitmask();
}
