package us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class InventoryPackets {

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        // Set cooldown
        itemRewriter.registerSetCooldown(0x18, 0x18, InventoryPackets::getNewItemId);

        // Set slot packet
        itemRewriter.registerSetSlot(Type.FLAT_ITEM, 0x17, 0x17);

        // Window items packet
        itemRewriter.registerWindowItems(Type.FLAT_ITEM_ARRAY, 0x15, 0x15);

        // Plugin message
        protocol.registerOutgoing(State.PLAY, 0x19, 0x19, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Channel
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.get(Type.STRING, 0);
                        if (channel.equals("minecraft:trader_list") || channel.equals("trader_list")) {
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                // Input Item
                                toClient(wrapper.passthrough(Type.FLAT_ITEM));
                                // Output Item
                                InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_ITEM));

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_ITEM));
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

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.FLAT_ITEM, 0x42, 0x42);

        // Declare Recipes
        protocol.registerOutgoing(State.PLAY, 0x54, 0x54, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int recipesNo = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < recipesNo; i++) {
                            wrapper.passthrough(Type.STRING); // Id
                            String type = wrapper.passthrough(Type.STRING);
                            if (type.equals("crafting_shapeless")) {
                                wrapper.passthrough(Type.STRING); // Group
                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
                                for (int i1 = 0; i1 < ingredientsNo; i1++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                                    for (int i2 = 0; i2 < items.length; i2++) {
                                        InventoryPackets.toClient(items[i2]);
                                    }
                                }
                                InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_ITEM)); // Result
                            } else if (type.equals("crafting_shaped")) {
                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                                wrapper.passthrough(Type.STRING); // Group
                                for (int i1 = 0; i1 < ingredientsNo; i1++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                                    for (int i2 = 0; i2 < items.length; i2++) {
                                        InventoryPackets.toClient(items[i2]);
                                    }
                                }
                                InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_ITEM)); // Result
                            } else if (type.equals("smelting")) {
                                wrapper.passthrough(Type.STRING); // Group
                                // Ingredient start
                                Item[] items = wrapper.passthrough(Type.FLAT_ITEM_ARRAY_VAR_INT);
                                for (int i2 = 0; i2 < items.length; i2++) {
                                    InventoryPackets.toClient(items[i2]);
                                }
                                // Ingredient end
                                InventoryPackets.toClient(wrapper.passthrough(Type.FLAT_ITEM));
                                wrapper.passthrough(Type.FLOAT); // EXP
                                wrapper.passthrough(Type.VAR_INT); // Cooking time
                            }
                        }
                    }
                });
            }
        });


        // Click window packet
        itemRewriter.registerClickWindow(Type.FLAT_ITEM, 0x08, 0x08);

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(Type.FLAT_ITEM, 0x24, 0x24);
    }

    public static void toClient(Item item) {
        if (item == null) return;
        item.setIdentifier(getNewItemId(item.getIdentifier()));
    }

    public static int getNewItemId(int itemId) {
        if (itemId >= 443) {
            return itemId + 5;
        }
        return itemId;
    }

    public static void toServer(Item item) {
        if (item == null) return;
        item.setIdentifier(getOldItemId(item.getIdentifier()));
    }

    public static int getOldItemId(int newId) {
        if (newId >= 448) {
            return newId - 5;
        }
        return newId;
    }
}
