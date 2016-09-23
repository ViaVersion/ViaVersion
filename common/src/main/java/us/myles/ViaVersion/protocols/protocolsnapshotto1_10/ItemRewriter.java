package us.myles.ViaVersion.protocols.protocolsnapshotto1_10;

import org.bukkit.Material;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.minecraft.item.Item;

public class ItemRewriter {
    public static void toClient(Item item) {
        if (hasEntityTag(item)) {
            CompoundTag entityTag = item.getTag().get("EntityTag");
            if (entityTag.get("id") instanceof StringTag) {
                StringTag id = entityTag.get("id");
                id.setValue("minecraft:" + id.getValue().toLowerCase());
            }
        }
    }

    public static void toServer(Item item) {
        if (hasEntityTag(item)) {
            CompoundTag entityTag = item.getTag().get("EntityTag");
            if (entityTag.get("id") instanceof StringTag) {
                StringTag id = entityTag.get("id");

                String value = id.getValue().replaceAll("minecraft:", "");
                if (value.length() > 1)
                    value = value.substring(0, 1).toUpperCase() + value.substring(1);
                id.setValue(value);
            }
        }
    }

    private static boolean hasEntityTag(Item item) {
        if (item != null && item.getId() == Material.MONSTER_EGG.getId()) {
            CompoundTag tag = item.getTag();
            if (tag != null && tag.contains("EntityTag") && tag.get("EntityTag") instanceof CompoundTag) {
                if (((CompoundTag) tag.get("EntityTag")).get("id") instanceof StringTag) {
                    return true;
                }
            }
        }
        return false;
    }
}
