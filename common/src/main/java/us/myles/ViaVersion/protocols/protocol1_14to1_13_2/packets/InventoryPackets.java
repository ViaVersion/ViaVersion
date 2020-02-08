package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.tag.builtin.*;
import com.google.common.collect.Sets;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.InventoryNameRewriter;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class InventoryPackets {
    private static final String NBT_TAG_NAME = "ViaVersion|" + Protocol1_14To1_13_2.class.getSimpleName();
    private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet("crafting_special_banneraddpattern", "crafting_special_repairitem");

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        // Set cooldown
        itemRewriter.registerSetCooldown(0x18, 0x17, InventoryPackets::getNewItemId);

        // Open Inventory
        protocol.registerOutgoing(State.PLAY, 0x14, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Short windowsId = wrapper.read(Type.UNSIGNED_BYTE);
                        String type = wrapper.read(Type.STRING);
                        String title = InventoryNameRewriter.processTranslate(wrapper.read(Type.STRING));
                        Short slots = wrapper.read(Type.UNSIGNED_BYTE);


                        if (type.equals("EntityHorse")) {
                            wrapper.setId(0x1F);
                            int entityId = wrapper.read(Type.INT);
                            wrapper.write(Type.UNSIGNED_BYTE, windowsId);
                            wrapper.write(Type.VAR_INT, slots.intValue());
                            wrapper.write(Type.INT, entityId);
                        } else {
                            wrapper.setId(0x2E);
                            wrapper.write(Type.VAR_INT, windowsId.intValue());

                            int typeId = -1;
                            switch (type) {
                                case "minecraft:container":
                                case "minecraft:chest":
                                    typeId = slots / 9 - 1;
                                    break;
                                case "minecraft:crafting_table":
                                    typeId = 11;
                                    break;
                                case "minecraft:furnace":
                                    typeId = 13;
                                    break;
                                case "minecraft:dropper":
                                case "minecraft:dispenser":
                                    typeId = 6;
                                    break;
                                case "minecraft:enchanting_table":
                                    typeId = 12;
                                    break;
                                case "minecraft:brewing_stand":
                                    typeId = 10;
                                    break;
                                case "minecraft:villager":
                                    typeId = 18;
                                    break;
                                case "minecraft:beacon":
                                    typeId = 8;
                                    break;
                                case "minecraft:anvil":
                                    typeId = 7;
                                    break;
                                case "minecraft:hopper":
                                    typeId = 15;
                                    break;
                                case "minecraft:shulker_box":
                                    typeId = 19;
                                    break;
                            }

                            if (typeId == -1) {
                                Via.getPlatform().getLogger().warning("Can't open inventory for 1.14 player! Type: " + type + " Size: " + slots);
                            }

                            wrapper.write(Type.VAR_INT, typeId);
                            wrapper.write(Type.STRING, title);
                        }
                    }
                });
            }
        });

        // Window items packet
        itemRewriter.registerWindowItems(Type.FLAT_VAR_INT_ITEM_ARRAY, 0x15, 0x14);

        // Set slot packet
        itemRewriter.registerSetSlot(Type.FLAT_VAR_INT_ITEM, 0x17, 0x16);

        // Plugin message
        protocol.registerOutgoing(State.PLAY, 0x19, 0x18, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Channel
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.get(Type.STRING, 0);
                        if (channel.equals("minecraft:trader_list") || channel.equals("trader_list")) {
                            wrapper.setId(0x27);
                            wrapper.resetReader();
                            wrapper.read(Type.STRING); // Remove channel

                            int windowId = wrapper.read(Type.INT);
                            wrapper.user().get(EntityTracker1_14.class).setLatestTradeWindowId(windowId);
                            wrapper.write(Type.VAR_INT, windowId);

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                // Input Item
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                // Output Item
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                }

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                                wrapper.write(Type.INT, 0);
                                wrapper.write(Type.INT, 0);
                                wrapper.write(Type.FLOAT, 0f);
                            }
                            wrapper.write(Type.VAR_INT, 0);
                            wrapper.write(Type.VAR_INT, 0);
                            wrapper.write(Type.BOOLEAN, false);
                        } else if (channel.equals("minecraft:book_open") || channel.equals("book_open")) {
                            int hand = wrapper.read(Type.VAR_INT);
                            wrapper.clearPacket();
                            wrapper.setId(0x2D);
                            wrapper.write(Type.VAR_INT, hand);
                        }
                    }
                });
            }
        });

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.FLAT_VAR_INT_ITEM, 0x42, 0x46);

        // Declare Recipes
        protocol.registerOutgoing(State.PLAY, 0x54, 0x5A, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int size = wrapper.passthrough(Type.VAR_INT);
                        int deleted = 0;
                        for (int i = 0; i < size; i++) {
                            String id = wrapper.read(Type.STRING); // Recipe Identifier
                            String type = wrapper.read(Type.STRING);
                            if (REMOVED_RECIPE_TYPES.contains(type)) {
                                deleted++;
                                continue;
                            }
                            wrapper.write(Type.STRING, type);
                            wrapper.write(Type.STRING, id);

                            if (type.equals("crafting_shapeless")) {
                                wrapper.passthrough(Type.STRING); // Group
                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
                                for (int j = 0; j < ingredientsNo; j++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                    for (Item item : items) toClient(item);
                                }
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
                            } else if (type.equals("crafting_shaped")) {
                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                                wrapper.passthrough(Type.STRING); // Group
                                for (int j = 0; j < ingredientsNo; j++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                    for (Item item : items) toClient(item);
                                }
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
                            } else if (type.equals("smelting")) {
                                wrapper.passthrough(Type.STRING); // Group
                                Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                for (Item item : items) toClient(item);
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                wrapper.passthrough(Type.FLOAT); // EXP
                                wrapper.passthrough(Type.VAR_INT); // Cooking time
                            }
                        }
                        wrapper.set(Type.VAR_INT, 0, size - deleted);
                    }
                });
            }
        });


        // Click window packet
        itemRewriter.registerClickWindow(Type.FLAT_VAR_INT_ITEM, 0x08, 0x09);

        // Select trade
        protocol.registerIncoming(State.PLAY, 0x1F, 0x21, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Selecting trade now moves the items, we need to resync the inventory
                        PacketWrapper resyncPacket = wrapper.create(0x08);
                        resyncPacket.write(Type.UNSIGNED_BYTE, ((short) wrapper.user().get(EntityTracker1_14.class).getLatestTradeWindowId())); // 0 - Window ID
                        resyncPacket.write(Type.SHORT, ((short) -999)); // 1 - Slot
                        resyncPacket.write(Type.BYTE, (byte) 2); // 2 - Button - End left click
                        resyncPacket.write(Type.SHORT, ((short) ThreadLocalRandom.current().nextInt())); // 3 - Action number
                        resyncPacket.write(Type.VAR_INT, 5); // 4 - Mode - Drag
                        CompoundTag tag = new CompoundTag("");
                        tag.put(new DoubleTag("force_resync", Double.NaN)); // Tags with NaN are not equal
                        resyncPacket.write(Type.FLAT_VAR_INT_ITEM, new Item(1, (byte) 1, (short) 0, tag)); // 5 - Clicked Item
                        resyncPacket.sendToServer(Protocol1_14To1_13_2.class, true, false);
                    }
                });
            }
        });

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(Type.FLAT_VAR_INT_ITEM, 0x24, 0x26);
    }


    public static void toClient(Item item) {
        if (item == null) return;
        item.setIdentifier(getNewItemId(item.getIdentifier()));

        CompoundTag tag;
        if ((tag = item.getTag()) != null) {
            // Display Lore now uses JSON
            Tag displayTag = tag.get("display");
            if (displayTag instanceof CompoundTag) {
                CompoundTag display = (CompoundTag) displayTag;
                Tag loreTag = display.get("Lore");
                if (loreTag instanceof ListTag) {
                    ListTag lore = (ListTag) loreTag;
                    display.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|Lore", ConverterRegistry.convertToValue(lore)));
                    for (Tag loreEntry : lore) {
                        if (loreEntry instanceof StringTag) {
                            ((StringTag) loreEntry).setValue(
                                    ChatRewriter.legacyTextToJson(
                                            ((StringTag) loreEntry).getValue()
                                    )
                            );
                        }
                    }
                }
            }
        }
    }

    public static int getNewItemId(int id) {
        Integer newId = MappingData.oldToNewItems.get(id);
        if (newId == null) {
            Via.getPlatform().getLogger().warning("Missing 1.14 item for 1.13.2 item " + id);
            return 1;
        }
        return newId;
    }

    public static void toServer(Item item) {
        if (item == null) return;
        item.setIdentifier(getOldItemId(item.getIdentifier()));

        CompoundTag tag;
        if ((tag = item.getTag()) != null) {
            // Display Name now uses JSON
            Tag displayTag = tag.get("display");
            if (displayTag instanceof CompoundTag) {
                CompoundTag display = (CompoundTag) displayTag;
                Tag loreTag = display.get("Lore");
                if (loreTag instanceof ListTag) {
                    ListTag lore = (ListTag) loreTag;
                    ListTag via = display.get(NBT_TAG_NAME + "|Lore");
                    if (via != null) {
                        display.put(ConverterRegistry.convertToTag("Lore", ConverterRegistry.convertToValue(via)));
                    } else {
                        for (Tag loreEntry : lore) {
                            if (loreEntry instanceof StringTag) {
                                ((StringTag) loreEntry).setValue(
                                        ChatRewriter.jsonTextToLegacy(
                                                ((StringTag) loreEntry).getValue()
                                        )
                                );
                            }
                        }
                    }
                    display.remove(NBT_TAG_NAME + "|Lore");
                }
            }
        }
    }

    public static int getOldItemId(int id) {
        Integer oldId = MappingData.oldToNewItems.inverse().get(id);
        return oldId != null ? oldId : 1;
    }
}
