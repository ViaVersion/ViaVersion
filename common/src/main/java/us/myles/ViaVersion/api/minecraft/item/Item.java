package us.myles.ViaVersion.api.minecraft.item;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @SerializedName(value = "identifier", alternate = "id")
    private int identifier;
    private byte amount;
    private short data;
    private CompoundTag tag;

    public Item(Item toCopy) {
        this(toCopy.getIdentifier(), toCopy.getAmount(), toCopy.getData(), toCopy.getTag());
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public byte getAmount() {
        return amount;
    }

    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public short getData() {
        return data;
    }

    public void setData(short data) {
        this.data = data;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        if (identifier != item.identifier) return false;
        if (amount != item.amount) return false;
        if (data != item.data) return false;
        return tag != null ? tag.equals(item.tag) : item.tag == null;
    }

    @Override
    public int hashCode() {
        int result = identifier;
        result = 31 * result + (int) amount;
        result = 31 * result + (int) data;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "identifier=" + identifier +
                ", amount=" + amount +
                ", data=" + data +
                ", tag=" + tag +
                '}';
    }
}
