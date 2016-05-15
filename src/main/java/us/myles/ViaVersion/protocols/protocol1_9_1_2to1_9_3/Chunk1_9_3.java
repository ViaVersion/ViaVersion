package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.spacehq.opennbt.tag.builtin.CompoundTag;

import java.util.List;

@Data
@AllArgsConstructor
public class Chunk1_9_3 {
    private int x;
    private int z;
    private boolean groundUp;
    private int bitmask;
    private byte[] sections;
    List<CompoundTag> blockEntities;

}
