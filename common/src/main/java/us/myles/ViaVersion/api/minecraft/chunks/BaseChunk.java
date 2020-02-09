package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class BaseChunk implements Chunk {
    protected final int x;
    protected final int z;
    protected final boolean groundUp;
    protected final int bitmask;
    protected final ChunkSection[] sections;
    protected int[] biomeData;
    protected CompoundTag heightMap;
    protected final List<CompoundTag> blockEntities;

    public BaseChunk(int x, int z, boolean groundUp, int bitmask, ChunkSection[] sections, int[] biomeData, List<CompoundTag> blockEntities) {
        this.x = x;
        this.z = z;
        this.groundUp = groundUp;
        this.bitmask = bitmask;
        this.sections = sections;
        this.biomeData = biomeData;
        this.blockEntities = blockEntities;
    }

    @Override
    public boolean isBiomeData() {
        return biomeData != null;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public boolean isGroundUp() {
        return groundUp;
    }

    @Override
    public int getBitmask() {
        return bitmask;
    }

    @Override
    public ChunkSection[] getSections() {
        return sections;
    }

    @Override
    public int[] getBiomeData() {
        return biomeData;
    }

    @Override
    public void setBiomeData(final int[] biomeData) {
        this.biomeData = biomeData;
    }

    @Override
    public CompoundTag getHeightMap() {
        return heightMap;
    }

    @Override
    public void setHeightMap(final CompoundTag heightMap) {
        this.heightMap = heightMap;
    }

    @Override
    public List<CompoundTag> getBlockEntities() {
        return blockEntities;
    }
}
