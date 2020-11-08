package us.myles.ViaVersion.protocols.protocol1_11_1to1_11.packets;

import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_11_1to1_11.Protocol1_11_1To1_11;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;

public class InventoryPackets {

    public static void register(Protocol1_11_1To1_11 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, item -> {}, InventoryPackets::toServerItem);
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_9_3.CREATIVE_INVENTORY_ACTION, Type.ITEM);
    }

    public static void toServerItem(Item item) {
        if (item == null) return;
        boolean invalid = item.getIdentifier() == 452;
        if (invalid) { // Stone
            item.setIdentifier((short) 1);
            item.setData((short) 0);
        }
    }

}
