package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.remapper.PacketHandler;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.type.Type;

public class InventoryPackets {
    public static void register(Protocol protocol) {
        // Window Property Packet
        protocol.registerOutgoing(State.PLAY, 0x31, 0x15, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Property Key
                map(Type.SHORT); // 2 - Property Value

                // TODO - Enchanting patch
            }
        });
        // Window Open Packet
        protocol.registerOutgoing(State.PLAY, 0x2D, 0x13, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.STRING); // 1 - Window Type
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 2 - Window Title
                map(Type.UNSIGNED_BYTE); // 3 - Slot Count
                // There is a horse parameter after this, we don't handle it and let it passthrough
                // TODO - Brewing patch
                // TODO - Save Inventory patch
            }
        });
        // Window Set Slot Packet
        protocol.registerOutgoing(State.PLAY, 0x2F, 0x16, new PacketRemapper() {

            @Override
            public void registerMap() {

                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM); // 2 - Slot Value
                // TODO Brewing patch
                // TODO - ItemStack rewriter
            }
        });
        // Window Set Slots Packet
        protocol.registerOutgoing(State.PLAY, 0x30, 0x14, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY); // 1 - Window Values

                // TODO Brewing patch
                // TODO - ItemStack rewriter
            }
        });
        // Close Window Packet
        protocol.registerOutgoing(State.PLAY, 0x2E, 0x12, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID

                // TODO Close Inventory patch
            }
        });

        // Map Packet
        protocol.registerOutgoing(State.PLAY, 0x34, 0x24, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Map ID
                map(Type.BYTE); // 1 - Map Scale
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        wrapper.write(Type.BOOLEAN, true); // 2 - Show marker
                    }
                });
                // Everything else is passed through
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x32, 0x11); // Confirm Transaction Packet

        /* Incoming Packets */

        // Creative Inventory Slot Action Packet
        protocol.registerIncoming(State.PLAY, 0x10, 0x18, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.SHORT);
                map(Type.ITEM);
                // TODO: Transform Item Patch
            }
        });

        // Player Click Window Packet
        protocol.registerIncoming(State.PLAY, 0x0E, 0x07, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE);
                map(Type.SHORT);
                map(Type.BYTE);
                map(Type.SHORT);
                map(Type.BYTE);
                map(Type.ITEM);
                // TODO: Transform Item Patch
                // TODO: Throw elytra and brewing patch
            }
        });

        // Close Window Incoming Packet
        protocol.registerIncoming(State.PLAY, 0x0D, 0x08, new PacketRemapper() {

            @Override
            public void registerMap() {
                // TODO Close Inventory patch
            }
        });



        // TODO Use Item

        /* Packets which do not have any field remapping or handlers */

        protocol.registerIncoming(State.PLAY, 0x0F, 0x05); // Confirm Transaction Packet
        protocol.registerIncoming(State.PLAY, 0x11, 0x06); // Enchant Item Packet

        // TODO Held Item change blocking patch
        protocol.registerIncoming(State.PLAY, 0x09, 0x17, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        System.out.println("held item");
                    }
                });
            }
        }); // Held Item Change Packet
    }
}
