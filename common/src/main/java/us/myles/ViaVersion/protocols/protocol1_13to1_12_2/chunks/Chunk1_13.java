package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;

import java.util.List;

@Data
@AllArgsConstructor
public class Chunk1_13 implements Chunk {
    private int x;
    private int z;
    private boolean groundUp;
    private int bitmask;
    private ChunkSection1_13[] sections;
    private byte[] biomeData;
    private List<CompoundTag> blockEntities;

    @Override
    public boolean isBiomeData() {
        return biomeData != null;
    }
}
