package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;

public class InventoryPackets {

    public static void register(Protocol1_16_2To1_16_1 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_16.COOLDOWN, InventoryPackets::getNewItemId);
        itemRewriter.registerWindowItems(ClientboundPackets1_16.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);

        protocol.registerOutgoing(ClientboundPackets1_16.UNLOCK_RECIPES, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BOOLEAN); // Open
                    wrapper.passthrough(Type.BOOLEAN); // Filter
                    wrapper.passthrough(Type.BOOLEAN); // Furnace
                    wrapper.passthrough(Type.BOOLEAN); // Filter furnace
                    // Blast furnace / smoker
                    wrapper.write(Type.BOOLEAN, false);
                    wrapper.write(Type.BOOLEAN, false);
                    wrapper.write(Type.BOOLEAN, false);
                    wrapper.write(Type.BOOLEAN, false);
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16.TRADE_LIST, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                    for (int i = 0; i < size; i++) {
                        Item input = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        toClient(input);

                        Item output = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        toClient(output);

                        if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                            toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));    // Second Item
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
                });
            }
        });

        itemRewriter.registerSetSlot(ClientboundPackets1_16.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerEntityEquipmentArray(ClientboundPackets1_16.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);

        protocol.registerOutgoing(ClientboundPackets1_16.DECLARE_RECIPES, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
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
                            case "smithing": {
                                wrapper.passthrough(Type.STRING);
                                Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                                for (Item item : items) toClient(item);
                                items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                                for (Item item : items) toClient(item);
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                break;
                            }
                        }
                    }
                });
            }
        });

        itemRewriter.registerClickWindow(ServerboundPackets1_16_2.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_16_2.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);
        protocol.registerIncoming(ServerboundPackets1_16_2.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });
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
            Via.getPlatform().getLogger().warning("Missing 1.16.2 item for 1.16 item " + id);
            return 1;
        }
        return newId;
    }

    public static int getOldItemId(int id) {
        int oldId = MappingData.oldToNewItems.inverse().get(id);
        return oldId != -1 ? oldId : 1;
    }
}
