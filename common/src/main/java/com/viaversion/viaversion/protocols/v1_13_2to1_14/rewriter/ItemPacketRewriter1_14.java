/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ItemPacketRewriter1_14 extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_14, Protocol1_13_2To1_14> {
    private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet("crafting_special_banneraddpattern", "crafting_special_repairitem");
    private static final JsonNBTComponentRewriter<ClientboundPackets1_13> COMPONENT_REWRITER = new JsonNBTComponentRewriter<>(null, JsonNBTComponentRewriter.ReadType.JSON) {
        @Override
        protected void handleTranslate(JsonObject object, String translate) {
            super.handleTranslate(object, translate);
            // Mojang decided to remove .name from inventory titles
            if (translate.startsWith("block.") && translate.endsWith(".name")) {
                object.addProperty("translate", translate.substring(0, translate.length() - 5));
            }
        }
    };

    public ItemPacketRewriter1_14(Protocol1_13_2To1_14 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_13.COOLDOWN);
        registerAdvancements(ClientboundPackets1_13.UPDATE_ADVANCEMENTS);

        protocol.registerClientbound(ClientboundPackets1_13.OPEN_SCREEN, null, wrapper -> {
            Short windowId = wrapper.read(Types.UNSIGNED_BYTE);
            String type = wrapper.read(Types.STRING);
            JsonElement title = wrapper.read(Types.COMPONENT);
            COMPONENT_REWRITER.processText(wrapper.user(), title);
            Short slots = wrapper.read(Types.UNSIGNED_BYTE);

            if (type.equals("EntityHorse")) {
                wrapper.setPacketType(ClientboundPackets1_14.HORSE_SCREEN_OPEN);
                int entityId = wrapper.read(Types.INT);
                wrapper.write(Types.UNSIGNED_BYTE, windowId);
                wrapper.write(Types.VAR_INT, slots.intValue());
                wrapper.write(Types.INT, entityId);
            } else {
                wrapper.setPacketType(ClientboundPackets1_14.OPEN_SCREEN);
                wrapper.write(Types.VAR_INT, windowId.intValue());

                int typeId = -1;
                switch (type) {
                    case "minecraft:crafting_table" -> typeId = 11;
                    case "minecraft:furnace" -> typeId = 13;
                    case "minecraft:dropper", "minecraft:dispenser" -> typeId = 6;
                    case "minecraft:enchanting_table" -> typeId = 12;
                    case "minecraft:brewing_stand" -> typeId = 10;
                    case "minecraft:villager" -> typeId = 18;
                    case "minecraft:beacon" -> typeId = 8;
                    case "minecraft:anvil" -> typeId = 7;
                    case "minecraft:hopper" -> typeId = 15;
                    case "minecraft:shulker_box" -> typeId = 19;
                    default -> {
                        if (slots > 0 && slots <= 54) {
                            typeId = slots / 9 - 1;
                        }
                    }
                }

                if (typeId == -1) {
                    protocol.getLogger().warning("Can't open inventory for player! Type: " + type + " Size: " + slots);
                }

                wrapper.write(Types.VAR_INT, typeId);
                wrapper.write(Types.COMPONENT, title);
            }
        });

        registerSetContent(ClientboundPackets1_13.CONTAINER_SET_CONTENT);
        registerSetSlot(ClientboundPackets1_13.CONTAINER_SET_SLOT);

        protocol.registerClientbound(ClientboundPackets1_13.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
                    if (channel.equals("minecraft:trader_list")) {
                        wrapper.setPacketType(ClientboundPackets1_14.MERCHANT_OFFERS);
                        wrapper.resetReader();
                        wrapper.read(Types.STRING); // Remove channel

                        int windowId = wrapper.read(Types.INT);
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
                        tracker.setLatestTradeWindowId(windowId);
                        wrapper.write(Types.VAR_INT, windowId);

                        int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            // Input Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                            // Output Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));

                            boolean secondItem = wrapper.passthrough(Types.BOOLEAN); // Has second item
                            if (secondItem) {
                                // Second Item
                                handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                            }

                            wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Types.INT); // Number of tools uses
                            wrapper.passthrough(Types.INT); // Maximum number of trade uses

                            wrapper.write(Types.INT, 0);
                            wrapper.write(Types.INT, 0);
                            wrapper.write(Types.FLOAT, 0f);
                        }
                        wrapper.write(Types.VAR_INT, 0);
                        wrapper.write(Types.VAR_INT, 0);
                        wrapper.write(Types.BOOLEAN, false);
                        wrapper.clearInputBuffer();
                    } else if (channel.equals("minecraft:book_open")) {
                        int hand = wrapper.read(Types.VAR_INT);
                        wrapper.clearPacket();
                        wrapper.setPacketType(ClientboundPackets1_14.OPEN_BOOK);
                        wrapper.write(Types.VAR_INT, hand);
                    }
                });
            }
        });

        registerSetEquippedItem(ClientboundPackets1_13.SET_EQUIPPED_ITEM);

        RecipeRewriter<ClientboundPackets1_13> recipeRewriter = new RecipeRewriter<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            int deleted = 0;
            for (int i = 0; i < size; i++) {
                String id = wrapper.read(Types.STRING); // Recipe Identifier
                String type = wrapper.read(Types.STRING);
                if (REMOVED_RECIPE_TYPES.contains(type)) {
                    deleted++;
                    continue;
                }
                wrapper.write(Types.STRING, type);
                wrapper.write(Types.STRING, id);

                recipeRewriter.handleRecipeType(wrapper, type);
            }
            wrapper.set(Types.VAR_INT, 0, size - deleted);
        });


        registerContainerClick(ServerboundPackets1_14.CONTAINER_CLICK);

        protocol.registerServerbound(ServerboundPackets1_14.SELECT_TRADE, wrapper -> {
            // Selecting trade now moves the items, we need to resync the inventory
            PacketWrapper resyncPacket = wrapper.create(ServerboundPackets1_13.CONTAINER_CLICK);
            EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
            resyncPacket.write(Types.BYTE, ((byte) tracker.getLatestTradeWindowId())); // 0 - Window ID
            resyncPacket.write(Types.SHORT, ((short) -999)); // 1 - Slot
            resyncPacket.write(Types.BYTE, (byte) 2); // 2 - Button - End left click
            resyncPacket.write(Types.SHORT, ((short) ThreadLocalRandom.current().nextInt())); // 3 - Action number
            resyncPacket.write(Types.VAR_INT, 5); // 4 - Mode - Drag
            CompoundTag tag = new CompoundTag();
            tag.put("force_resync", new DoubleTag(Double.NaN)); // Tags with NaN are not equal
            resyncPacket.write(Types.ITEM1_13_2, new DataItem(1, (byte) 1, tag)); // 5 - Clicked Item
            resyncPacket.scheduleSendToServer(Protocol1_13_2To1_14.class);
        });

        registerSetCreativeModeSlot(ServerboundPackets1_14.SET_CREATIVE_MODE_SLOT);
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item == null) return null;
        item.setIdentifier(Protocol1_13_2To1_14.MAPPINGS.getNewItemId(item.identifier()));

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
        item.setIdentifier(Protocol1_13_2To1_14.MAPPINGS.getOldItemId(item.identifier()));

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
