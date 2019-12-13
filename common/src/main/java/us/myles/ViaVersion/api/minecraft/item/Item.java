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
    @SerializedName(value = "identifier", alternate = "id")
    private int identifier;
    private byte amount;
    private short data;
    private CompoundTag tag;

    public Item(Item toCopy) {
        this(toCopy.getIdentifier(), toCopy.getAmount(), toCopy.getData(), toCopy.getTag());
    }
}
