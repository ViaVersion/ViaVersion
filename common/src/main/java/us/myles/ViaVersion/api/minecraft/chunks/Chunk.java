package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

import java.util.List;

public interface Chunk {

    int getX();

    int getZ();

    boolean isBiomeData();

    boolean isFullChunk();

    @Deprecated
    default boolean isGroundUp() {
        return isFullChunk();
    }

    int getBitmask();

    ChunkSection[] getSections();

    int[] getBiomeData();

    void setBiomeData(int[] biomeData);

    CompoundTag getHeightMap();

    void setHeightMap(CompoundTag heightMap);

    List<CompoundTag> getBlockEntities();
}
