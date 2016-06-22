package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
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
    List<CompoundTag> blockEntities;

    public boolean isBiomeData() {
        return biomeData != null;
    }
}
