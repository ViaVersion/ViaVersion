package us.myles.ViaVersion.api.minecraft.item;

import lombok.*;
import org.bukkit.inventory.ItemStack;
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

    /**
     * Create an item from a bukkit stack (doesn't save NBT)
     *
     * @param stack The stack to convert from
     * @return The output stack
     */
    public static Item getItem(ItemStack stack) {
        if (stack == null) return null;
        return new Item((short) stack.getTypeId(), (byte) stack.getAmount(), stack.getDurability(), null);
    }
}
