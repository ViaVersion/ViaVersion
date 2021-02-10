package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public interface Chunk {

    int getX();

    int getZ();

    /**
     * @return whether this chunk holds biome data, always true for 1.17+ chunks
     */
    boolean isBiomeData();

    /**
     * @return whether this is a full chunk, always true for 1.17+ chunks
     */
    boolean isFullChunk();

    @Deprecated
    default boolean isGroundUp() {
        return isFullChunk();
    }

    boolean isIgnoreOldLightData();

    void setIgnoreOldLightData(boolean ignoreOldLightData);

    /**
     * @return chunk section bit mask for chunks < 1.17
     * @see #getChunkMask()
     */
    int getBitmask();

    void setBitmask(int bitmask);

    /**
     * @return chunk section bit mask, only non-null available for 1.17+ chunks
     * @see #getBitmask()
     */
    @Nullable
    BitSet getChunkMask();

    void setChunkMask(BitSet chunkSectionMask);

    ChunkSection[] getSections();

    void setSections(ChunkSection[] sections);

    @Nullable
    int[] getBiomeData();

    void setBiomeData(int[] biomeData);

    CompoundTag getHeightMap();

    void setHeightMap(CompoundTag heightMap);

    List<CompoundTag> getBlockEntities();
}
