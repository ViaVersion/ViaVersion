package us.myles.ViaVersion.api.minecraft.item;

import lombok.*;
import org.spacehq.opennbt.tag.builtin.CompoundTag;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Item {
    private short id;
    private byte amount;
    private short data;
    private CompoundTag tag;
}
