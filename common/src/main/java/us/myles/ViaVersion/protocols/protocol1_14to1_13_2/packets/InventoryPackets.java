package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ComponentRewriter;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.rewriters.RecipeRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.RecipeRewriter1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class InventoryPackets {
    private static final String NBT_TAG_NAME = "ViaVersion|" + Protocol1_14To1_13_2.class.getSimpleName();
    private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet("crafting_special_banneraddpattern", "crafting_special_repairitem");
    private static final ComponentRewriter COMPONENT_REWRITER = new ComponentRewriter() {
        @Override
        protected void handleTranslate(JsonObject object, String translate) {
            super.handleTranslate(object, translate);
            // Mojang decided to remove .name from inventory titles
            if (translate.startsWith("block.") && translate.endsWith(".name")) {
                object.addProperty("translate", translate.substring(0, translate.length() - 5));
            }
        }
    };

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_13.COOLDOWN);
        itemRewriter.registerAdvancements(ClientboundPackets1_13.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);

        protocol.registerOutgoing(ClientboundPackets1_13.OPEN_WINDOW, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Short windowsId = wrapper.read(Type.UNSIGNED_BYTE);
                        String type = wrapper.read(Type.STRING);
                        JsonElement title = wrapper.read(Type.COMPONENT);
                        COMPONENT_REWRITER.processText(title);
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
                            wrapper.write(Type.COMPONENT, title);
                        }
                    }
                });
            }
        });

        itemRewriter.registerWindowItems(ClientboundPackets1_13.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        itemRewriter.registerSetSlot(ClientboundPackets1_13.SET_SLOT, Type.FLAT_VAR_INT_ITEM);

        protocol.registerOutgoing(ClientboundPackets1_13.PLUGIN_MESSAGE, new PacketRemapper() {
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

        itemRewriter.registerEntityEquipment(ClientboundPackets1_13.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);

        RecipeRewriter recipeRewriter = new RecipeRewriter1_13_2(protocol, InventoryPackets::toClient);
        protocol.registerOutgoing(ClientboundPackets1_13.DECLARE_RECIPES, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
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

                        recipeRewriter.handle(wrapper, type);
                    }
                    wrapper.set(Type.VAR_INT, 0, size - deleted);
                });
            }
        });


        itemRewriter.registerClickWindow(ServerboundPackets1_14.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);

        protocol.registerIncoming(ServerboundPackets1_14.SELECT_TRADE, new PacketRemapper() {
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

        itemRewriter.registerCreativeInvAction(ServerboundPackets1_14.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        itemRewriter.registerSpawnParticle(ClientboundPackets1_13.SPAWN_PARTICLE, Type.FLAT_VAR_INT_ITEM, Type.FLOAT);
    }

    public static void toClient(Item item) {
        if (item == null) return;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getNewItemId(item.getIdentifier()));

        if (item.getTag() == null) return;

        // Display Lore now uses JSON
        Tag displayTag = item.getTag().get("display");
        if (displayTag instanceof CompoundTag) {
            CompoundTag display = (CompoundTag) displayTag;
            Tag loreTag = display.get("Lore");
            if (loreTag instanceof ListTag) {
                ListTag lore = (ListTag) loreTag;
                display.put(new ListTag(NBT_TAG_NAME + "|Lore", lore.clone().getValue())); // Save old lore
                for (Tag loreEntry : lore) {
                    if (loreEntry instanceof StringTag) {
                        String jsonText = ChatRewriter.legacyTextToJsonString(((StringTag) loreEntry).getValue(), true);
                        ((StringTag) loreEntry).setValue(jsonText);
                    }
                }
            }
        }
    }

    public static void toServer(Item item) {
        if (item == null) return;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getOldItemId(item.getIdentifier()));

        if (item.getTag() == null) return;

        // Display Name now uses JSON
        Tag displayTag = item.getTag().get("display");
        if (displayTag instanceof CompoundTag) {
            CompoundTag display = (CompoundTag) displayTag;
            Tag loreTag = display.get("Lore");
            if (loreTag instanceof ListTag) {
                ListTag lore = (ListTag) loreTag;
                ListTag savedLore = display.remove(NBT_TAG_NAME + "|Lore");
                if (savedLore != null) {
                    display.put(new ListTag("Lore", savedLore.getValue()));
                } else {
                    for (Tag loreEntry : lore) {
                        if (loreEntry instanceof StringTag) {
                            ((StringTag) loreEntry).setValue(ChatRewriter.jsonToLegacyText(((StringTag) loreEntry).getValue()));
                        }
                    }
                }
            }
        }
    }
}