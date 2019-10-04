package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.BedRewriter;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;

public class InventoryPackets {

    public static void register(Protocol1_12To1_11_1 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, BedRewriter::toClientItem, BedRewriter::toServerItem);

        // Set slot packet
        itemRewriter.registerSetSlot(Type.ITEM, 0x16, 0x16);

        // Window items packet
        itemRewriter.registerWindowItems(Type.ITEM_ARRAY, 0x14, 0x14);

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.ITEM, 0x3C, 0x3E);

        // Plugin message Packet -> Trading
        protocol.registerOutgoing(State.PLAY, 0x18, 0x18, new PacketRemapper() {
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
                                if (secondItem)
                                    BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Second Item

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });


        // Click window packet
        protocol.registerIncoming(State.PLAY, 0x07, 0x08, new PacketRemapper() {
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
        itemRewriter.registerCreativeInvAction(Type.ITEM, 0x18, 0x1b);
    }
}
