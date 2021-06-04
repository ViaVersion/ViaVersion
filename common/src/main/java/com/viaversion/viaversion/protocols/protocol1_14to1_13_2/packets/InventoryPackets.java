/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.RecipeRewriter1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class InventoryPackets extends ItemRewriter<Protocol1_14To1_13_2> {
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

    public InventoryPackets(Protocol1_14To1_13_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_13.COOLDOWN);
        registerAdvancements(ClientboundPackets1_13.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);

        protocol.registerClientbound(ClientboundPackets1_13.OPEN_WINDOW, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Short windowId = wrapper.read(Type.UNSIGNED_BYTE);
                        String type = wrapper.read(Type.STRING);
                        JsonElement title = wrapper.read(Type.COMPONENT);
                        COMPONENT_REWRITER.processText(title);
                        Short slots = wrapper.read(Type.UNSIGNED_BYTE);

                        if (type.equals("EntityHorse")) {
                            wrapper.setId(0x1F);
                            int entityId = wrapper.read(Type.INT);
                            wrapper.write(Type.UNSIGNED_BYTE, windowId);
                            wrapper.write(Type.VAR_INT, slots.intValue());
                            wrapper.write(Type.INT, entityId);
                        } else {
                            wrapper.setId(0x2E);
                            wrapper.write(Type.VAR_INT, windowId.intValue());

                            int typeId = -1;
                            switch (type) {
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
                                case "minecraft:container":
                                case "minecraft:chest":
                                default:
                                    if (slots > 0 && slots <= 54) {
                                        typeId = slots / 9 - 1;
                                    }
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

        registerWindowItems(ClientboundPackets1_13.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        registerSetSlot(ClientboundPackets1_13.SET_SLOT, Type.FLAT_VAR_INT_ITEM);

        protocol.registerClientbound(ClientboundPackets1_13.PLUGIN_MESSAGE, new PacketRemapper() {
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
                            EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                            tracker.setLatestTradeWindowId(windowId);
                            wrapper.write(Type.VAR_INT, windowId);

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                // Input Item
                                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                // Output Item
                                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
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

        registerEntityEquipment(ClientboundPackets1_13.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);

        RecipeRewriter recipeRewriter = new RecipeRewriter1_13_2(protocol);
        protocol.registerClientbound(ClientboundPackets1_13.DECLARE_RECIPES, new PacketRemapper() {
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


        registerClickWindow(ServerboundPackets1_14.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);

        protocol.registerServerbound(ServerboundPackets1_14.SELECT_TRADE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Selecting trade now moves the items, we need to resync the inventory
                        PacketWrapper resyncPacket = wrapper.create(0x08);
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                        resyncPacket.write(Type.UNSIGNED_BYTE, ((short) tracker.getLatestTradeWindowId())); // 0 - Window ID
                        resyncPacket.write(Type.SHORT, ((short) -999)); // 1 - Slot
                        resyncPacket.write(Type.BYTE, (byte) 2); // 2 - Button - End left click
                        resyncPacket.write(Type.SHORT, ((short) ThreadLocalRandom.current().nextInt())); // 3 - Action number
                        resyncPacket.write(Type.VAR_INT, 5); // 4 - Mode - Drag
                        CompoundTag tag = new CompoundTag();
                        tag.put("force_resync", new DoubleTag(Double.NaN)); // Tags with NaN are not equal
                        resyncPacket.write(Type.FLAT_VAR_INT_ITEM, new DataItem(1, (byte) 1, (short) 0, tag)); // 5 - Clicked Item
                        resyncPacket.scheduleSendToServer(Protocol1_14To1_13_2.class);
                    }
                });
            }
        });

        registerCreativeInvAction(ServerboundPackets1_14.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        registerSpawnParticle(ClientboundPackets1_13.SPAWN_PARTICLE, Type.FLAT_VAR_INT_ITEM, Type.FLOAT);
    }

    @Override
    public Item handleItemToClient(Item item) {
        if (item == null) return null;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getNewItemId(item.identifier()));

        if (item.tag() == null) return item;

        // Display Lore now uses JSON
        Tag displayTag = item.tag().get("display");
        if (displayTag instanceof CompoundTag) {
            CompoundTag display = (CompoundTag) displayTag;
            Tag loreTag = display.get("Lore");
            if (loreTag instanceof ListTag) {
                ListTag lore = (ListTag) loreTag;
                display.put(NBT_TAG_NAME + "|Lore", new ListTag(lore.clone().getValue())); // Save old lore
                for (Tag loreEntry : lore) {
                    if (loreEntry instanceof StringTag) {
                        String jsonText = ChatRewriter.legacyTextToJsonString(((StringTag) loreEntry).getValue(), true);
                        ((StringTag) loreEntry).setValue(jsonText);
                    }
                }
            }
        }
        return item;
    }

    @Override
    public Item handleItemToServer(Item item) {
        if (item == null) return null;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getOldItemId(item.identifier()));

        if (item.tag() == null) return item;

        // Display Name now uses JSON
        Tag displayTag = item.tag().get("display");
        if (displayTag instanceof CompoundTag) {
            CompoundTag display = (CompoundTag) displayTag;
            Tag loreTag = display.get("Lore");
            if (loreTag instanceof ListTag) {
                ListTag lore = (ListTag) loreTag;
                ListTag savedLore = display.remove(NBT_TAG_NAME + "|Lore");
                if (savedLore != null) {
                    display.put("Lore", new ListTag(savedLore.getValue()));
                } else {
                    for (Tag loreEntry : lore) {
                        if (loreEntry instanceof StringTag) {
                            ((StringTag) loreEntry).setValue(ChatRewriter.jsonToLegacyText(((StringTag) loreEntry).getValue()));
                        }
                    }
                }
            }
        }
        return item;
    }
}