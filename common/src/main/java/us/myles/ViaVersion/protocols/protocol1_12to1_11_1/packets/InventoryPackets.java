package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.EntityIdRewriter;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.BedRewriter;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;

public class InventoryPackets {

    public static void register(Protocol1_12To1_11_1 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, BedRewriter::toClientItem, InventoryPackets::toServerItem);

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
                                BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Input Item
                                BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Output Item

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Second Item
                                }

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });


        protocol.registerIncoming(ServerboundPackets1_12.CLICK_WINDOW, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.UNSIGNED_BYTE); // 0 - Window ID
                        map(Type.SHORT); // 1 - Slot
                        map(Type.BYTE); // 2 - Button
                        map(Type.SHORT); // 3 - Action number
                        map(Type.VAR_INT); // 4 - Mode
                        map(Type.ITEM); // 5 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                                    BedRewriter.toServerItem(item);
                                    return;
                                }
                                byte button = wrapper.get(Type.BYTE, 0);
                                int mode = wrapper.get(Type.VAR_INT, 0);
                                // QUICK_MOVE PATCH (Shift + (click/double click))
                                if (mode == 1 && button == 0 && item == null) {
                                    short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                                    short slotId = wrapper.get(Type.SHORT, 0);
                                    short actionId = wrapper.get(Type.SHORT, 1);
                                    InventoryQuickMoveProvider provider = Via.getManager().getProviders().get(InventoryQuickMoveProvider.class);
                                    boolean succeed = provider.registerQuickMoveAction(windowId, slotId, actionId, wrapper.user());
                                    if (succeed) {
                                        wrapper.cancel();
                                    }
                                    // otherwise just pass through so the server sends the PacketPlayOutTransaction packet.
                                } else {
                                    BedRewriter.toServerItem(item);
                                }
                            }
                        });
                    }
                }
        );

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_12.CREATIVE_INVENTORY_ACTION, Type.ITEM);
    }

    public static void toServerItem(Item item) {
        BedRewriter.toServerItem(item);
        if (item == null) return;
        boolean invalid = item.getIdentifier() >= 235 && item.getIdentifier() <= 252;
        invalid |= item.getIdentifier() == 453;
        if (invalid) { // Stone
            item.setIdentifier((short) 1);
            item.setData((short) 0);
        }
    }
}
