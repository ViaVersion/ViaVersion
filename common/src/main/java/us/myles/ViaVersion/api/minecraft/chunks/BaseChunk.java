package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class BaseChunk implements Chunk {
    protected int x;
    protected int z;
    protected boolean groundUp;
    protected int bitmask;
    protected ChunkSection[] sections;
    protected int[] biomeData;
    protected CompoundTag heightMap;
    protected List<CompoundTag> blockEntities;

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
}
