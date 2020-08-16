package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.RecipeRewriter1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;

public class InventoryPackets {

    public static void register(Protocol1_15To1_14_4 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_14.COOLDOWN);
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
        item.setIdentifier(Protocol1_15To1_14_4.MAPPINGS.getNewItemId(item.getIdentifier()));
    }

    public static void toServer(Item item) {
        if (item == null) return;
        item.setIdentifier(Protocol1_15To1_14_4.MAPPINGS.getOldItemId(item.getIdentifier()));
    }
}
