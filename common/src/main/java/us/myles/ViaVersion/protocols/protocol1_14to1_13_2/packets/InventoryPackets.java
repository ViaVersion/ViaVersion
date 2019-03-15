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
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.WindowTracker;

import java.util.Set;

public class InventoryPackets {
    private static String NBT_TAG_NAME;
    private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet("crafting_special_banneraddpattern", "crafting_special_repairitem");

    public static void register(Protocol protocol) {
        NBT_TAG_NAME = "ViaVersion|" + protocol.getClass().getSimpleName();
        /*
            Outgoing packets
         */

        // Close window
        protocol.registerOutgoing(State.PLAY, 0x13, 0x13, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        if (wrapper.passthrough(Type.UNSIGNED_BYTE) == tracker.getChestId()) {
                            tracker.reset();
                        }
                    }
                });
            }
        });

        // Open Inventory
        protocol.registerOutgoing(State.PLAY, 0x14, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        Short windowsId = wrapper.read(Type.UNSIGNED_BYTE);
                        String type = wrapper.read(Type.STRING);
                        String title = wrapper.read(Type.STRING);
                        Short slots = wrapper.read(Type.UNSIGNED_BYTE);
                        tracker.reset();

                        if (type.equals("EntityHorse")) {
                            wrapper.setId(0x14);
                            int entityId = wrapper.read(Type.INT);
                            wrapper.write(Type.UNSIGNED_BYTE, windowsId);
                            wrapper.write(Type.VAR_INT, slots.intValue());
                            wrapper.write(Type.INT, entityId);
                        } else {
                            wrapper.setId(0x58);
                            wrapper.write(Type.VAR_INT, windowsId.intValue());

                            int typeId = -1;
                            switch (type) {
                                case "minecraft:container":
                                case "minecraft:chest":
                                    typeId = slots / 9 - 1;
                                    if (typeId < 0 || typeId > 5) {
                                        slots = (short) (slots / 9 * 9);
                                        tracker.setCurrentChestSize(slots.intValue());
                                        tracker.setChestId(windowsId);
                                        if (slots == 0) {
                                            typeId = 0;
                                        } else {
                                            typeId = 5;
                                        }
                                    }
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
        protocol.registerOutgoing(State.PLAY, 0x15, 0x15, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.FLAT_VAR_INT_ITEM_ARRAY); // 1 - Window Values

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item[] stacks = wrapper.get(Type.FLAT_VAR_INT_ITEM_ARRAY, 0);
                        for (Item stack : stacks) toClient(stack);

                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        if (tracker.getChestId() == wrapper.get(Type.UNSIGNED_BYTE, 0)) {
                            int size = tracker.getCurrentChestSize();
                            if (size != -1) {
                                boolean oversized = size != 0;
                                int clientSize = size == 0 ? 9 : 54;
                                int totalPages = 1;
                                int start = 0;
                                int length = size;
                                if (oversized) {
                                    totalPages = (int) Math.ceil(size / 45f);
                                    start = tracker.getCurrentPage() * 45;
                                    length = Math.min(size - (tracker.getCurrentPage() * 45), 45);
                                }
                                Item[] newItems = new Item[stacks.length - size + clientSize];
                                System.arraycopy(stacks, start, newItems, 0, length);
                                for (int i = length; i < clientSize; i++) {
                                    if (i == 53 && oversized && totalPages - 1 != tracker.getCurrentPage()) {
                                        CompoundTag tag = new CompoundTag("");
                                        CompoundTag display = new CompoundTag("display");
                                        display.put(new StringTag("Name", "{\"text\":\"Next Page\"}"));
                                        addPlayerHeadData(tag, "0dcc0bfa-66eb-4f2f-8413-9f98e109dfa4",
                                                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp" +
                                                        "7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI" +
                                                        "2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk" +
                                                        "3ZTkxZDQzYyJ9fX0=");
                                        tag.put(display);
                                        newItems[i] = new Item(768, (byte) 1, (short) 0, tag); // Head
                                    } else if (i == 45 && oversized && 0 != tracker.getCurrentPage()) {
                                        CompoundTag tag = new CompoundTag("");
                                        CompoundTag display = new CompoundTag("display");
                                        display.put(new StringTag("Name", "{\"text\":\"Previous Page\"}"));
                                        addPlayerHeadData(tag, "e287aeb5-f89b-4e85-a7cc-bb9a3751fbf6",
                                                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp" +
                                                        "7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjM" +
                                                        "wYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI" +
                                                        "4ODFhYWNjNyJ9fX0=");
                                        tag.put(display);
                                        newItems[i] = new Item(768, (byte) 1, (short) 0, tag); // Head
                                    } else {
                                        CompoundTag tag = new CompoundTag("");
                                        CompoundTag display = new CompoundTag("display");
                                        display.put(new StringTag("Name", "{\"text\":\"\"}"));
                                        tag.put(display);
                                        newItems[i] = new Item(346, (byte) 1, (short) 0, tag); // Black stained glass panel
                                    }
                                }
                                System.arraycopy(stacks, size, newItems, clientSize, stacks.length - size);
                                wrapper.set(Type.FLAT_VAR_INT_ITEM_ARRAY, 0, newItems);
                            }
                        }
                    }
                });
            }
        });

        // Set slot packet
        protocol.registerOutgoing(State.PLAY, 0x17, 0x17, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.FLAT_VAR_INT_ITEM); // 2 - Slot Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        int size = tracker.getCurrentChestSize();
                        int slot = wrapper.get(Type.SHORT, 0);
                        if (size != -1) {
                            boolean oversized = size != 0;
                            int clientSize = size == 0 ? 9 : 54;
                            int totalPages = 1;
                            int start = 0;
                            int length = size;
                            if (oversized) {
                                totalPages = (int) Math.ceil(size / 45f);
                                start = tracker.getCurrentPage() * 45;
                                length = Math.min(size - (tracker.getCurrentPage() * 45), 45);
                            }
                            if (slot >= 0 && slot < start) {
                                wrapper.cancel();
                                return;
                            }
                            if (slot >= start) {
                                if (slot >= start + length) {
                                    if (slot < size) {
                                        wrapper.cancel();
                                        return;
                                    } else {
                                        wrapper.set(Type.SHORT, 0, (short) (slot - size + clientSize));
                                    }
                                } else {
                                    wrapper.set(Type.SHORT, 0, (short) (slot - start));
                                }
                            }
                        }

                        toClient(wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                    }
                });
            }
        });

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
                            wrapper.setId(0x59);
                            wrapper.resetReader();
                            wrapper.read(Type.STRING); // Remove channel

                            int windowId = wrapper.read(Type.INT);
                            WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                            tracker.reset();
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
                            wrapper.read(Type.REMAINING_BYTES);
                            int hand = wrapper.read(Type.VAR_INT);
                            wrapper.clearPacket();
                            wrapper.setId(0x2C);
                            wrapper.write(Type.VAR_INT, hand);
                        }
                    }
                });
            }
        });

        // Entity Equipment Packet
        protocol.registerOutgoing(State.PLAY, 0x42, 0x42, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.FLAT_VAR_INT_ITEM); // 2 - Item

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        toClient(wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                    }
                });
            }
        });

        // Declare Recipes
        protocol.registerOutgoing(State.PLAY, 0x54, 0x55, new PacketRemapper() {
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


        /*
            Incoming packets
         */

        // Click window packet
        protocol.registerIncoming(State.PLAY, 0x08, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(Type.FLAT_VAR_INT_ITEM); // 5 - Clicked Item

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short slot = wrapper.get(Type.SHORT, 0);
                        byte button = wrapper.get(Type.BYTE, 0);
                        int mode = wrapper.get(Type.VAR_INT, 0);
                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        if (tracker.getChestId() == wrapper.get(Type.UNSIGNED_BYTE, 0)) {
                            int size = tracker.getCurrentChestSize();
                            if (size != -1) {
                                boolean oversized = size != 0;
                                int clientSize = size == 0 ? 9 : 54;
                                int totalPages = 1;
                                int start = 0;
                                int length = size;
                                if (oversized) {
                                    totalPages = (int) Math.ceil(size / 45f);
                                    start = tracker.getCurrentPage() * 45;
                                    length = Math.min(size - (tracker.getCurrentPage() * 45), 45);
                                }
                                if (slot >= 0) {
                                    if ((slot >= length && slot < clientSize) || mode == 1 || mode == 6) {
                                        // Force resync when the click isn't in the server screen
                                        // or is shift click or double click (they may get/put the items from/to another page)
                                        CompoundTag tag = new CompoundTag("");
                                        tag.put(new DoubleTag("resync", Double.NaN)); // Tags with NaN are not equal
                                        wrapper.set(Type.FLAT_VAR_INT_ITEM, 0, new Item(1, (byte) 1, (short) 0, tag));
                                    }
                                    if (slot < length) {
                                        wrapper.set(Type.SHORT, 0, slot = (short) (slot + start));
                                    } else if (slot >= length && slot < clientSize) {
                                        if (slot == 5 * 9 && tracker.getCurrentPage() != 0) { // Previous page
                                            tracker.setCurrentPage(tracker.getCurrentPage() - 1);
                                        } else if (slot == 6 * 9 - 1 && tracker.getCurrentPage() + 1 < totalPages) {
                                            tracker.setCurrentPage(tracker.getCurrentPage() + 1);
                                        }
                                        wrapper.set(Type.VAR_INT, 0, mode = 5); // Drag mode
                                        wrapper.set(Type.BYTE, 0, button = (byte) 2); // Stop left-click drag
                                        wrapper.set(Type.SHORT, 0, slot = (short) -999);
                                    } else {
                                        wrapper.set(Type.SHORT, 0, slot = (short) (slot - (clientSize - size)));
                                    }
                                }
                            }
                        }
                        toServer(wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                    }
                });
            }
        });

        // Close window
        protocol.registerIncoming(State.PLAY, 0x09, 0x0A, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        WindowTracker tracker = wrapper.user().get(WindowTracker.class);
                        if (tracker.getChestId() == wrapper.passthrough(Type.UNSIGNED_BYTE)) {
                            tracker.reset();
                        }
                    }
                });
            }
        });

        // Creative Inventory Action
        protocol.registerIncoming(State.PLAY, 0x24, 0x26, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot
                map(Type.FLAT_VAR_INT_ITEM); // 1 - Clicked Item

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        toServer(wrapper.get(Type.FLAT_VAR_INT_ITEM, 0));
                    }
                });
            }
        });
    }


    public static void toClient(Item item) {
        if (item == null) return;
        item.setIdentifier(getNewItemId(item.getIdentifier()));

        CompoundTag tag;
        if ((tag = item.getTag()) != null) {
            // Display Lore now uses JSON
            if (tag.get("display") instanceof CompoundTag) {
                CompoundTag display = tag.get("display");
                if (display.get("Lore") instanceof ListTag) {
                    ListTag lore = display.get("Lore");
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
            if (tag.get("display") instanceof CompoundTag) {
                CompoundTag display = tag.get("display");
                if (((CompoundTag) tag.get("display")).get("Lore") instanceof ListTag) {
                    ListTag lore = display.get("Lore");
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

    private static void addPlayerHeadData(CompoundTag tag, String ownerId, String textureValue) {
        CompoundTag skullOwner = new CompoundTag("SkullOwner");
        CompoundTag properties = new CompoundTag("Properties");
        ListTag textures = new ListTag("textures", CompoundTag.class);
        CompoundTag texture = new CompoundTag("");
        texture.put(new StringTag("Value", textureValue));
        skullOwner.put(new StringTag("Id", ownerId));
        skullOwner.put(properties);
        textures.add(texture);
        properties.put(textures);
        tag.put(skullOwner);
    }
}
