/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class InventoryPackets extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_14, Protocol1_14To1_13_2> {
    private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet("crafting_special_banneraddpattern", "crafting_special_repairitem");
    private static final ComponentRewriter<ClientboundPackets1_13> COMPONENT_REWRITER = new ComponentRewriter<ClientboundPackets1_13>(null, ComponentRewriter.ReadType.JSON) {
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
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_13.COOLDOWN);
        registerAdvancements(ClientboundPackets1_13.ADVANCEMENTS);

        protocol.registerClientbound(ClientboundPackets1_13.OPEN_WINDOW, null, wrapper -> {
            Short windowId = wrapper.read(Type.UNSIGNED_BYTE);
            String type = wrapper.read(Type.STRING);
            JsonElement title = wrapper.read(Type.COMPONENT);
            COMPONENT_REWRITER.processText(wrapper.user(), title);
            Short slots = wrapper.read(Type.UNSIGNED_BYTE);

            if (type.equals("EntityHorse")) {
                wrapper.setPacketType(ClientboundPackets1_14.OPEN_HORSE_WINDOW);
                int entityId = wrapper.read(Type.INT);
                wrapper.write(Type.UNSIGNED_BYTE, windowId);
                wrapper.write(Type.VAR_INT, slots.intValue());
                wrapper.write(Type.INT, entityId);
            } else {
                wrapper.setPacketType(ClientboundPackets1_14.OPEN_WINDOW);
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
        });

        registerWindowItems(ClientboundPackets1_13.WINDOW_ITEMS);
        registerSetSlot(ClientboundPackets1_13.SET_SLOT);

        protocol.registerClientbound(ClientboundPackets1_13.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    String channel = Key.namespaced(wrapper.get(Type.STRING, 0));
                    if (channel.equals("minecraft:trader_list")) {
                        wrapper.setPacketType(ClientboundPackets1_14.TRADE_LIST);
                        wrapper.resetReader();
                        wrapper.read(Type.STRING); // Remove channel

                        int windowId = wrapper.read(Type.INT);
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
                        tracker.setLatestTradeWindowId(windowId);
                        wrapper.write(Type.VAR_INT, windowId);

                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            // Input Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2));
                            // Output Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2));

                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                            if (secondItem) {
                                // Second Item
                                handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2));
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
                        wrapper.clearInputBuffer();
                    } else if (channel.equals("minecraft:book_open")) {
                        int hand = wrapper.read(Type.VAR_INT);
                        wrapper.clearPacket();
                        wrapper.setPacketType(ClientboundPackets1_14.OPEN_BOOK);
                        wrapper.write(Type.VAR_INT, hand);
                    }
                });
            }
        });

        registerEntityEquipment(ClientboundPackets1_13.ENTITY_EQUIPMENT);

        RecipeRewriter<ClientboundPackets1_13> recipeRewriter = new RecipeRewriter<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_13.DECLARE_RECIPES, wrapper -> {
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

                recipeRewriter.handleRecipeType(wrapper, type);
            }
            wrapper.set(Type.VAR_INT, 0, size - deleted);
        });


        registerClickWindow(ServerboundPackets1_14.CLICK_WINDOW);

        protocol.registerServerbound(ServerboundPackets1_14.SELECT_TRADE, wrapper -> {
            // Selecting trade now moves the items, we need to resync the inventory
            PacketWrapper resyncPacket = wrapper.create(ServerboundPackets1_13.CLICK_WINDOW);
            EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_14To1_13_2.class);
            resyncPacket.write(Type.UNSIGNED_BYTE, ((short) tracker.getLatestTradeWindowId())); // 0 - Window ID
            resyncPacket.write(Type.SHORT, ((short) -999)); // 1 - Slot
            resyncPacket.write(Type.BYTE, (byte) 2); // 2 - Button - End left click
            resyncPacket.write(Type.SHORT, ((short) ThreadLocalRandom.current().nextInt())); // 3 - Action number
            resyncPacket.write(Type.VAR_INT, 5); // 4 - Mode - Drag
            CompoundTag tag = new CompoundTag();
            tag.put("force_resync", new DoubleTag(Double.NaN)); // Tags with NaN are not equal
            resyncPacket.write(Type.ITEM1_13_2, new DataItem(1, (byte) 1, (short) 0, tag)); // 5 - Clicked Item
            resyncPacket.scheduleSendToServer(Protocol1_14To1_13_2.class);
        });

        registerCreativeInvAction(ServerboundPackets1_14.CREATIVE_INVENTORY_ACTION);

        registerSpawnParticle(ClientboundPackets1_13.SPAWN_PARTICLE, Type.FLOAT);
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item == null) return null;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getNewItemId(item.identifier()));

        if (item.tag() == null) return item;

        // Display Lore now uses JSON
        CompoundTag display = item.tag().getCompoundTag("display");
        if (display != null) {
            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore != null) {
                display.put(nbtTagName("Lore"), lore.copy()); // Save old lore
                for (StringTag loreEntry : lore) {
                    String jsonText = ComponentUtil.legacyToJsonString(loreEntry.getValue(), true);
                    loreEntry.setValue(jsonText);
                }
            }
        }
        return item;
    }

    @Override
    public Item handleItemToServer(UserConnection connection, Item item) {
        if (item == null) return null;
        item.setIdentifier(Protocol1_14To1_13_2.MAPPINGS.getOldItemId(item.identifier()));

        if (item.tag() == null) return item;

        // Display Name now uses JSON
        CompoundTag display = item.tag().getCompoundTag("display");
        if (display != null) {
            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore != null) {
                Tag savedLore = display.remove(nbtTagName("Lore"));
                if (savedLore instanceof ListTag) {
                    display.put("Lore", savedLore.copy());
                } else {
                    for (StringTag loreEntry : lore) {
                        loreEntry.setValue(ComponentUtil.jsonToLegacy(loreEntry.getValue()));
                    }
                }
            }
        }
        return item;
    }
}
