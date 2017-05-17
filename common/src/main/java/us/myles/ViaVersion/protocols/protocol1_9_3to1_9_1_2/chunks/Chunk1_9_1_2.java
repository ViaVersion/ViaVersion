package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;

import java.util.List;

@Data
@AllArgsConstructor
public class Chunk1_9_1_2 implements Chunk {
    private int x;
    private int z;
    private boolean groundUp;
    private int bitmask;
    private final ChunkSection1_9_1_2[] sections;
    private byte[] biomeData;
    private List<CompoundTag> blockEntities;

    public boolean isBiomeData() {
        return biomeData != null;
    }
}
