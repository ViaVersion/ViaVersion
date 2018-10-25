package us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Chunk {
    protected int x;
    protected int z;
    protected boolean groundUp;
    protected int bitmask;
    protected ChunkSection[] sections;
    protected byte[] biomeData;
    protected List<CompoundTag> blockEntities;

    public boolean isBiomeData() {
        return biomeData != null;
    }
}
