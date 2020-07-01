package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.RecipeRewriter1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;

public class InventoryPackets {

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_14.COOLDOWN, InventoryPackets::getNewItemId);
        itemRewriter.registerWindowItems(ClientboundPackets1_14.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        itemRewriter.registerTradeList(ClientboundPackets1_14.TRADE_LIST, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerSetSlot(ClientboundPackets1_14.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerEntityEquipment(ClientboundPackets1_14.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerAdvancements(ClientboundPackets1_14.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);

        new RecipeRewriter1_14(protocol, InventoryPackets::toClient).registerDefaultHandler(ClientboundPackets1_14.DECLARE_RECIPES);

        itemRewriter.registerClickWindow(ServerboundPackets1_14.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_14.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);
    }

    public static void toClient(Item item) {
        if (item == null) return;
        item.setIdentifier(getNewItemId(item.getIdentifier()));
    }

    public static void toServer(Item item) {
        if (item == null) return;
        item.setIdentifier(getOldItemId(item.getIdentifier()));
    }

    public static int getNewItemId(int id) {
        int newId = MappingData.oldToNewItems.get(id);
        if (newId == -1) {
            Via.getPlatform().getLogger().warning("Missing 1.15 item for 1.14 item " + id);
            return 1;
        }
        return newId;
    }

    public static int getOldItemId(int id) {
        int oldId = MappingData.oldToNewItems.inverse().get(id);
        return oldId != -1 ? oldId : 1;
    }
}
