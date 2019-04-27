package us.myles.ViaVersion.api.minecraft.item;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Item {
    public static final Item EMPTY = new Item((short) 0, (byte) 0, (short) 0, null);
    @SerializedName(value = "identifier", alternate = "id")
    private int identifier;
    private byte amount;
    private short data;
    private CompoundTag tag;

    @Deprecated
    public short getId() {
        return (short) identifier;
    }

    @Deprecated
    public void setId(short id) {
        identifier = id;
    }

    @Deprecated
    public Item(short id, byte amount, short data, CompoundTag tag) {
        this.identifier = id;
        this.amount = amount;
        this.data = data;
        this.tag = tag;
    }
}
