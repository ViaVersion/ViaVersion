package us.myles.ViaVersion.protocols.protocol1_11to1_10.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.EntityIdRewriter;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;

public class InventoryPackets {

    public static void register(Protocol1_11To1_10 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, EntityIdRewriter::toClientItem, EntityIdRewriter::toServerItem);

        // Set slot packet
        itemRewriter.registerSetSlot(Type.ITEM, 0x16, 0x16);

        // Window items packet
        itemRewriter.registerWindowItems(Type.ITEM_ARRAY, 0x14, 0x14);

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.ITEM, 0x3C, 0x3C);

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


        // Click window packet
        itemRewriter.registerClickWindow(Type.ITEM, 0x07, 0x07);

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(Type.ITEM, 0x18, 0x18);
    }
}
