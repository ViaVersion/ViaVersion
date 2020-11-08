package us.myles.ViaVersion.protocols.protocol1_11to1_10.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.EntityIdRewriter;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;

public class InventoryPackets {

    public static void register(Protocol1_11To1_10 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, EntityIdRewriter::toClientItem, InventoryPackets::toServerItem);

        itemRewriter.registerSetSlot(ClientboundPackets1_9_3.SET_SLOT, Type.ITEM);
        itemRewriter.registerWindowItems(ClientboundPackets1_9_3.WINDOW_ITEMS, Type.ITEM_ARRAY);
        itemRewriter.registerEntityEquipment(ClientboundPackets1_9_3.ENTITY_EQUIPMENT, Type.ITEM);

        // Plugin message Packet -> Trading
        protocol.registerOutgoing(ClientboundPackets1_9_3.PLUGIN_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.STRING, 0).equalsIgnoreCase("MC|TrList")) {
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                EntityIdRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Input Item
                                EntityIdRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Output Item

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem)
                                    EntityIdRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Second Item

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });

        itemRewriter.registerClickWindow(ServerboundPackets1_9_3.CLICK_WINDOW, Type.ITEM);
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_9_3.CREATIVE_INVENTORY_ACTION, Type.ITEM);
    }

    public static void toServerItem(Item item) {
        EntityIdRewriter.toServerItem(item);
        if (item == null) return;
        boolean invalid = item.getIdentifier() >= 218 && item.getIdentifier() <= 234;
        invalid |= item.getIdentifier() == 449 || item.getIdentifier() == 450;
        if (invalid) { // Stone
            item.setIdentifier((short) 1);
            item.setData((short) 0);
        }
    }

}
