package us.myles.ViaVersion2.api.item;

import lombok.Getter;
import lombok.Setter;
import org.spacehq.opennbt.tag.builtin.CompoundTag;

@Getter
@Setter
public class Item {
    private short id;
    private byte amount;
    private short data;
    private CompoundTag tag;
}
