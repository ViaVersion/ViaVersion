package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;

public class InventoryPackets {

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        // Set cooldown
        itemRewriter.registerSetCooldown(0x17, 0x18, InventoryPackets::getNewItemId);

        // Window items packet
        itemRewriter.registerWindowItems(Type.FLAT_VAR_INT_ITEM_ARRAY, 0x14, 0x15);

        // Trade list packet
        protocol.registerOutgoing(State.PLAY, 0x27, 0x28, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.passthrough(Type.VAR_INT);
                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            Item input = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                            toClient(input);

                            Item output = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                            toClient(output);

                            if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                                // Second Item
                                Item second = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                                toClient(second);
                            }

                            wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Type.INT); // Number of tools uses
                            wrapper.passthrough(Type.INT); // Maximum number of trade uses

                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.FLOAT);
                            wrapper.passthrough(Type.INT);
                        }

                        wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.BOOLEAN);
                    }
                });
            }
        });

        // Set slot packet
        itemRewriter.registerSetSlot(Type.FLAT_VAR_INT_ITEM, 0x16, 0x17);

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.FLAT_VAR_INT_ITEM, 0x46, 0x47);

        // Declare Recipes
        protocol.registerOutgoing(State.PLAY, 0x5A, 0x5B, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int size = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < size; i++) {
                            String type = wrapper.passthrough(Type.STRING).replace("minecraft:", "");
                            String id = wrapper.passthrough(Type.STRING);

                            switch (type) {
                                case "crafting_shapeless": {
                                    wrapper.passthrough(Type.STRING); // Group

                                    int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
                                    for (int j = 0; j < ingredientsNo; j++) {
                                        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                        for (Item item : items) toClient(item);
                                    }
                                    toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result

                                    break;
                                }
                                case "crafting_shaped": {
                                    int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                                    wrapper.passthrough(Type.STRING); // Group

                                    for (int j = 0; j < ingredientsNo; j++) {
                                        Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                        for (Item item : items) toClient(item);
                                    }
                                    toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result

                                    break;
                                }
                                case "blasting":
                                case "smoking":
                                case "campfire_cooking":
                                case "smelting": {
                                    wrapper.passthrough(Type.STRING); // Group

                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients

                                    for (Item item : items) toClient(item);
                                    toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                    wrapper.passthrough(Type.FLOAT); // EXP

                                    wrapper.passthrough(Type.VAR_INT); // Cooking time

                                    break;
                                }
                                case "stonecutting": {
                                    wrapper.passthrough(Type.STRING);
                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                    for (Item item : items) toClient(item);
                                    toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });

        // Click window packet
        itemRewriter.registerClickWindow(Type.FLAT_VAR_INT_ITEM, 0x09, 0x09);

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(Type.FLAT_VAR_INT_ITEM, 0x26, 0x26);
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
        Integer newId = MappingData.oldToNewItems.get(id);
        if (newId == null) {
            Via.getPlatform().getLogger().warning("Missing 1.15 item for 1.14 item " + id);
            return 1;
        }
        return newId;
    }

    public static int getOldItemId(int id) {
        Integer oldId = MappingData.oldToNewItems.inverse().get(id);
        return oldId != null ? oldId : 1;
    }
}
