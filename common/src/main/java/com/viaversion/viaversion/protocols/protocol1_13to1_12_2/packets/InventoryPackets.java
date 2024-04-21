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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SoundSource;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.IdAndData;
import com.viaversion.viaversion.util.Key;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InventoryPackets extends ItemRewriter<ClientboundPackets1_12_1, ServerboundPackets1_13, Protocol1_13To1_12_2> {

    public InventoryPackets(Protocol1_13To1_12_2 protocol) {
        super(protocol, null, null);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_12_1.SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM1_8, Type.ITEM1_13); // 2 - Slot Value

                handler(wrapper -> handleItemToClient(wrapper.user(), wrapper.get(Type.ITEM1_13, 0)));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.WINDOW_ITEMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM1_8_SHORT_ARRAY, Type.ITEM1_13_SHORT_ARRAY); // 1 - Window Values

                handler(wrapper -> {
                    Item[] items = wrapper.get(Type.ITEM1_13_SHORT_ARRAY, 0);
                    for (Item item : items) {
                        handleItemToClient(wrapper.user(), item);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.WINDOW_PROPERTY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.SHORT); // Property
                map(Type.SHORT); // Value

                handler(wrapper -> {
                    short property = wrapper.get(Type.SHORT, 0);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        wrapper.set(Type.SHORT, 1, (short) protocol.getMappingData().getEnchantmentMappings().getNewId(wrapper.get(Type.SHORT, 1)));
                    }
                });
            }
        });

        // Plugin message Packet -> Trading
        protocol.registerClientbound(ClientboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Channel

                handlerSoftFail(wrapper -> {
                    String channel = wrapper.get(Type.STRING, 0);
                    // Handle stopsound change
                    if (channel.equals("MC|StopSound")) {
                        String originalSource = wrapper.read(Type.STRING);
                        String originalSound = wrapper.read(Type.STRING);

                        // Reset the packet
                        wrapper.clearPacket();
                        wrapper.setPacketType(ClientboundPackets1_13.STOP_SOUND);

                        byte flags = 0;
                        wrapper.write(Type.BYTE, flags); // Placeholder
                        if (!originalSource.isEmpty()) {
                            flags |= 1;
                            Optional<SoundSource> finalSource = SoundSource.findBySource(originalSource);
                            if (!finalSource.isPresent()) {
                                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                    Via.getPlatform().getLogger().info("Could not handle unknown sound source " + originalSource + " falling back to default: master");
                                }
                                finalSource = Optional.of(SoundSource.MASTER);

                            }

                            wrapper.write(Type.VAR_INT, finalSource.get().getId());
                        }
                        if (!originalSound.isEmpty()) {
                            flags |= 2;
                            wrapper.write(Type.STRING, originalSound);
                        }

                        wrapper.set(Type.BYTE, 0, flags); // Update flags
                        return;
                    } else if (channel.equals("MC|TrList")) {
                        channel = "minecraft:trader_list";
                        wrapper.passthrough(Type.INT); // Passthrough Window ID

                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            // Input Item
                            Item input = wrapper.read(Type.ITEM1_8);
                            handleItemToClient(wrapper.user(), input);
                            wrapper.write(Type.ITEM1_13, input);
                            // Output Item
                            Item output = wrapper.read(Type.ITEM1_8);
                            handleItemToClient(wrapper.user(), output);
                            wrapper.write(Type.ITEM1_13, output);

                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                            if (secondItem) {
                                // Second Item
                                Item second = wrapper.read(Type.ITEM1_8);
                                handleItemToClient(wrapper.user(), second);
                                wrapper.write(Type.ITEM1_13, second);
                            }

                            wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Type.INT); // Number of tools uses
                            wrapper.passthrough(Type.INT); // Maximum number of trade uses
                        }
                    } else {
                        String old = channel;
                        channel = getNewPluginChannelId(channel);
                        if (channel == null) {
                            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                Via.getPlatform().getLogger().warning("Ignoring clientbound plugin message with channel: " + old);
                            }
                            wrapper.cancel();
                            return;
                        } else if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                            String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                            List<String> rewrittenChannels = new ArrayList<>();
                            for (String s : channels) {
                                String rewritten = getNewPluginChannelId(s);
                                if (rewritten != null) {
                                    rewrittenChannels.add(rewritten);
                                } else if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                    Via.getPlatform().getLogger().warning("Ignoring plugin channel in clientbound " + Key.stripMinecraftNamespace(channel).toUpperCase(Locale.ROOT) + ": " + s);
                                }
                            }
                            if (!rewrittenChannels.isEmpty()) {
                                wrapper.write(Type.REMAINING_BYTES, Joiner.on('\0').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                            } else {
                                wrapper.cancel();
                                return;
                            }
                        }
                    }
                    wrapper.set(Type.STRING, 0, channel);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.ENTITY_EQUIPMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM1_8, Type.ITEM1_13); // 2 - Item

                handler(wrapper -> handleItemToClient(wrapper.user(), wrapper.get(Type.ITEM1_13, 0)));
            }
        });


        protocol.registerServerbound(ServerboundPackets1_13.CLICK_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(Type.ITEM1_13, Type.ITEM1_8); // 5 - Clicked Item

                handler(wrapper -> handleItemToServer(wrapper.user(), wrapper.get(Type.ITEM1_8, 0)));
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    String channel = wrapper.get(Type.STRING, 0);
                    String old = channel;
                    channel = getOldPluginChannelId(channel);
                    if (channel == null) {
                        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                            Via.getPlatform().getLogger().warning("Ignoring serverbound plugin message with channel: " + old);
                        }
                        wrapper.cancel();
                        return;
                    } else if (channel.equals("REGISTER") || channel.equals("UNREGISTER")) {
                        String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                        List<String> rewrittenChannels = new ArrayList<>();
                        for (String s : channels) {
                            String rewritten = getOldPluginChannelId(s);
                            if (rewritten != null) {
                                rewrittenChannels.add(rewritten);
                            } else if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                Via.getPlatform().getLogger().warning("Ignoring plugin channel in serverbound " + channel + ": " + s);
                            }
                        }
                        wrapper.write(Type.REMAINING_BYTES, Joiner.on('\0').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                    }
                    wrapper.set(Type.STRING, 0, channel);
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.SHORT); // 0 - Slot
                map(Type.ITEM1_13, Type.ITEM1_8); // 1 - Clicked Item

                handler(wrapper -> handleItemToServer(wrapper.user(), wrapper.get(Type.ITEM1_8, 0)));
            }
        });
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item == null) return null;
        CompoundTag tag = item.tag();

        // Save original id
        int originalId = (item.identifier() << 16 | item.data() & 0xFFFF);

        int rawId = IdAndData.toRawData(item.identifier(), item.data());

        // NBT Additions
        if (isDamageable(item.identifier())) {
            if (tag == null) item.setTag(tag = new CompoundTag());
            tag.put("Damage", new IntTag(item.data()));
        }
        if (item.identifier() == 358) { // map
            if (tag == null) item.setTag(tag = new CompoundTag());
            tag.put("map", new IntTag(item.data()));
        }

        // NBT Changes
        if (tag != null) {
            // Invert banner/shield color id
            boolean banner = item.identifier() == 425;
            if (banner || item.identifier() == 442) {
                CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
                if (blockEntityTag != null) {
                    NumberTag baseTag = blockEntityTag.getNumberTag("Base");
                    if (baseTag != null) {
                        // Set banner item id according to nbt
                        if (banner) {
                            rawId = 6800 + baseTag.asInt();
                        }

                        blockEntityTag.putInt("Base", 15 - baseTag.asInt());
                    }

                    ListTag<CompoundTag> patternsTag = blockEntityTag.getListTag("Patterns", CompoundTag.class);
                    if (patternsTag != null) {
                        for (CompoundTag pattern : patternsTag) {
                            NumberTag colorTag = pattern.getNumberTag("Color");
                            if (colorTag == null) {
                                continue;
                            }

                            // Invert color id
                            pattern.putInt("Color", 15 - colorTag.asInt());
                        }
                    }
                }
            }
            // Display Name now uses JSON
            CompoundTag display = tag.getCompoundTag("display");
            if (display != null) {
                StringTag name = display.getStringTag("Name");
                if (name != null) {
                    display.putString(nbtTagName("Name"), name.getValue());
                    name.setValue(ComponentUtil.legacyToJsonString(name.getValue(), true));
                }
            }
            // ench is now Enchantments and now uses identifiers
            ListTag<CompoundTag> ench = tag.getListTag("ench", CompoundTag.class);
            if (ench != null) {
                ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);
                for (CompoundTag enchEntry : ench) {
                    NumberTag idTag = enchEntry.getNumberTag("id");
                    if (idTag == null) {
                        continue;
                    }

                    CompoundTag enchantmentEntry = new CompoundTag();
                    short oldId = idTag.asShort();
                    String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                    if (newId == null) {
                        newId = "viaversion:legacy/" + oldId;
                    }
                    enchantmentEntry.putString("id", newId);

                    NumberTag levelTag = enchEntry.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchantmentEntry.putShort("lvl", levelTag.asShort());
                    }

                    enchantments.add(enchantmentEntry);
                }
                tag.remove("ench");
                tag.put("Enchantments", enchantments);
            }

            ListTag<CompoundTag> storedEnch = tag.getListTag("StoredEnchantments", CompoundTag.class);
            if (storedEnch != null) {
                ListTag<CompoundTag> newStoredEnch = new ListTag<>(CompoundTag.class);
                for (CompoundTag enchEntry : storedEnch) {
                    NumberTag idTag = enchEntry.getNumberTag("id");
                    if (idTag == null) {
                        continue;
                    }

                    CompoundTag enchantmentEntry = new CompoundTag();
                    short oldId = idTag.asShort();
                    String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                    if (newId == null) {
                        newId = "viaversion:legacy/" + oldId;
                    }
                    enchantmentEntry.putString("id", newId);

                    NumberTag levelTag = enchEntry.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchantmentEntry.putShort("lvl", levelTag.asShort());
                    }

                    newStoredEnch.add(enchantmentEntry);
                }
                tag.put("StoredEnchantments", newStoredEnch);
            }

            ListTag<?> canPlaceOnTag = tag.getListTag("CanPlaceOn");
            if (canPlaceOnTag != null) {
                ListTag<StringTag> newCanPlaceOn = new ListTag<>(StringTag.class);
                tag.put(nbtTagName("CanPlaceOn"), canPlaceOnTag.copy());
                for (Tag oldTag : canPlaceOnTag) {
                    Object value = oldTag.getValue();
                    String oldId = Key.stripMinecraftNamespace(value.toString());
                    String numberConverted = BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
                    if (numberConverted != null) {
                        oldId = numberConverted;
                    }
                    String[] newValues = BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT));
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag(newValue));
                        }
                    } else {
                        newCanPlaceOn.add(new StringTag(oldId.toLowerCase(Locale.ROOT)));
                    }
                }
                tag.put("CanPlaceOn", newCanPlaceOn);
            }

            ListTag<?> canDestroyTag = tag.getListTag("CanDestroy");
            if (canDestroyTag != null) {
                ListTag<StringTag> newCanDestroy = new ListTag<>(StringTag.class);
                tag.put(nbtTagName("CanDestroy"), canDestroyTag.copy());
                for (Tag oldTag : canDestroyTag) {
                    Object value = oldTag.getValue();
                    String oldId = Key.stripMinecraftNamespace(value.toString());
                    String numberConverted = BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
                    if (numberConverted != null) {
                        oldId = numberConverted;
                    }
                    String[] newValues = BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT));
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag(newValue));
                        }
                    } else {
                        newCanDestroy.add(new StringTag(oldId.toLowerCase(Locale.ROOT)));
                    }
                }
                tag.put("CanDestroy", newCanDestroy);
            }
            // Handle SpawnEggs
            if (item.identifier() == 383) {
                CompoundTag entityTag = tag.getCompoundTag("EntityTag");
                if (entityTag != null) {
                    StringTag idTag = entityTag.getStringTag("id");
                    if (idTag != null) {
                        rawId = SpawnEggRewriter.getSpawnEggId(idTag.getValue());
                        if (rawId == -1) {
                            rawId = 25100288; // Bat fallback
                        } else {
                            entityTag.remove("id");
                            if (entityTag.isEmpty()) {
                                tag.remove("EntityTag");
                            }
                        }
                    } else {
                        // Fallback to bat
                        rawId = 25100288;
                    }
                } else {
                    // Fallback to bat
                    rawId = 25100288;
                }
            }
            if (tag.isEmpty()) {
                item.setTag(tag = null);
            }
        }

        if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(rawId) == -1) {
            if (!isDamageable(item.identifier()) && item.identifier() != 358) { // Map
                if (tag == null) item.setTag(tag = new CompoundTag());
                tag.put(nbtTagName(), new IntTag(originalId)); // Data will be lost, saving original id
            }
            if (item.identifier() == 31 && item.data() == 0) { // Shrub was removed
                rawId = IdAndData.toRawData(32); // Dead Bush
            } else if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(IdAndData.removeData(rawId)) != -1) {
                rawId = IdAndData.removeData(rawId);
            } else {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.identifier());
                }
                rawId = 16; // Stone
            }
        }

        item.setIdentifier(Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(rawId));
        item.setData((short) 0);
        return item;
    }

    public static String getNewPluginChannelId(String old) {
        // Default channels that should not be modifiable
        switch (old) {
            case "MC|TrList":
                return "minecraft:trader_list";
            case "MC|Brand":
                return "minecraft:brand";
            case "MC|BOpen":
                return "minecraft:book_open";
            case "MC|DebugPath":
                return "minecraft:debug/paths";
            case "MC|DebugNeighborsUpdate":
                return "minecraft:debug/neighbors_update";
            case "REGISTER":
                return "minecraft:register";
            case "UNREGISTER":
                return "minecraft:unregister";
            case "BungeeCord":
                return "bungeecord:main";
            case "bungeecord:main":
                return null;
            default:
                String mappedChannel = Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().get(old);
                if (mappedChannel != null) return mappedChannel;
                return MappingData.validateNewChannel(old);
        }
    }

    @Override
    public Item handleItemToServer(UserConnection connection, Item item) {
        if (item == null) return null;

        Integer rawId = null;
        boolean gotRawIdFromTag = false;

        CompoundTag tag = item.tag();

        // Use tag to get original ID and data
        if (tag != null) {
            // Check for valid tag
            NumberTag viaTag = tag.getNumberTag(nbtTagName());
            if (viaTag != null) {
                rawId = viaTag.asInt();
                // Remove the tag
                tag.remove(nbtTagName());
                gotRawIdFromTag = true;
            }
        }

        if (rawId == null) {
            int oldId = Protocol1_13To1_12_2.MAPPINGS.getItemMappings().inverse().getNewId(item.identifier());
            if (oldId != -1) {
                // Handle spawn eggs
                Optional<String> eggEntityId = SpawnEggRewriter.getEntityId(oldId);
                if (eggEntityId.isPresent()) {
                    rawId = 383 << 16;
                    if (tag == null)
                        item.setTag(tag = new CompoundTag());
                    if (!tag.contains("EntityTag")) {
                        CompoundTag entityTag = new CompoundTag();
                        entityTag.put("id", new StringTag(eggEntityId.get()));
                        tag.put("EntityTag", entityTag);
                    }
                } else {
                    rawId = IdAndData.getId(oldId) << 16 | oldId & 0xF;
                }
            }
        }

        if (rawId == null) {
            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Failed to get 1.12 item for " + item.identifier());
            }
            rawId = 0x10000; // Stone
        }

        item.setIdentifier((short) (rawId >> 16));
        item.setData((short) (rawId & 0xFFFF));

        // NBT changes
        if (tag != null) {
            if (isDamageable(item.identifier())) {
                NumberTag damageTag = tag.getNumberTag("Damage");
                if (damageTag != null) {
                    if (!gotRawIdFromTag) {
                        item.setData(damageTag.asShort());
                    }
                    tag.remove("Damage");
                }
            }

            if (item.identifier() == 358) { // map
                NumberTag mapTag = tag.getNumberTag("map");
                if (mapTag != null) {
                    if (!gotRawIdFromTag) {
                        item.setData(mapTag.asShort());
                    }
                    tag.remove("map");
                }
            }

            if (item.identifier() == 442 || item.identifier() == 425) { // shield / banner
                CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
                if (blockEntityTag != null) {
                    NumberTag baseTag = blockEntityTag.getNumberTag("Base");
                    if (baseTag != null) {
                        blockEntityTag.putInt("Base", 15 - baseTag.asInt()); // invert color id
                    }

                    ListTag<CompoundTag> patternsTag = blockEntityTag.getListTag("Patterns", CompoundTag.class);
                    if (patternsTag != null) {
                        for (CompoundTag pattern : patternsTag) {
                            NumberTag colorTag = pattern.getNumberTag("Color");
                            pattern.putInt("Color", 15 - colorTag.asInt()); // Invert color id
                        }
                    }
                }
            }
            // Display Name now uses JSON
            CompoundTag display = tag.getCompoundTag("display");
            if (display != null) {
                StringTag name = display.getStringTag("Name");
                if (name != null) {
                    Tag via = display.remove(nbtTagName("Name"));
                    name.setValue(via instanceof StringTag ? (String) via.getValue() : ComponentUtil.jsonToLegacy(name.getValue()));
                }
            }

            // ench is now Enchantments and now uses identifiers
            ListTag<CompoundTag> enchantments = tag.getListTag("Enchantments", CompoundTag.class);
            if (enchantments != null) {
                ListTag<CompoundTag> ench = new ListTag<>(CompoundTag.class);
                for (CompoundTag enchantmentEntry : enchantments) {
                    StringTag idTag = enchantmentEntry.getStringTag("id");
                    if (idTag == null) {
                        continue;
                    }

                    CompoundTag enchEntry = new CompoundTag();
                    String newId = idTag.getValue();
                    Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                    if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                        oldId = Short.valueOf(newId.substring(18));
                    }
                    if (oldId != null) {
                        enchEntry.putShort("id", oldId);
                        NumberTag levelTag = enchantmentEntry.getNumberTag("lvl");
                        if (levelTag != null) {
                            enchEntry.putShort("lvl", levelTag.asShort());
                        }
                        ench.add(enchEntry);
                    }
                }
                tag.remove("Enchantments");
                tag.put("ench", ench);
            }


            ListTag<CompoundTag> storedEnch = tag.getListTag("StoredEnchantments", CompoundTag.class);
            if (storedEnch != null) {
                ListTag<CompoundTag> newStoredEnch = new ListTag<>(CompoundTag.class);
                for (CompoundTag enchantmentEntry : storedEnch) {
                    StringTag idTag = enchantmentEntry.getStringTag("id");
                    if (idTag == null) {
                        continue;
                    }

                    CompoundTag enchEntry = new CompoundTag();
                    String newId = idTag.getValue();
                    Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                    if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                        oldId = Short.valueOf(newId.substring(18));
                    }

                    if (oldId == null) {
                        continue;
                    }

                    enchEntry.putShort("id", oldId);
                    NumberTag levelTag = enchantmentEntry.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchEntry.putShort("lvl", levelTag.asShort());
                    }
                    newStoredEnch.add(enchEntry);
                }
                tag.put("StoredEnchantments", newStoredEnch);
            }
            if (tag.getListTag(nbtTagName("CanPlaceOn")) != null) {
                tag.put("CanPlaceOn", tag.remove(nbtTagName("CanPlaceOn")));
            } else if (tag.getListTag("CanPlaceOn") != null) {
                ListTag<?> old = tag.getListTag("CanPlaceOn");
                ListTag<StringTag> newCanPlaceOn = new ListTag<>(StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? Key.stripMinecraftNamespace((String) value)
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag(newValue));
                        }
                    } else {
                        newCanPlaceOn.add(new StringTag(value.toString()));
                    }
                }
                tag.put("CanPlaceOn", newCanPlaceOn);
            }
            if (tag.getListTag(nbtTagName("CanDestroy")) != null) {
                tag.put("CanDestroy", tag.remove(nbtTagName("CanDestroy")));
            } else if (tag.getListTag("CanDestroy") != null) {
                ListTag<?> old = tag.getListTag("CanDestroy");
                ListTag<StringTag> newCanDestroy = new ListTag<>(StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? Key.stripMinecraftNamespace((String) value)
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag(newValue));
                        }
                    } else {
                        newCanDestroy.add(new StringTag(oldTag.getValue().toString()));
                    }
                }
                tag.put("CanDestroy", newCanDestroy);
            }
        }
        return item;
    }

    public static String getOldPluginChannelId(String newId) {
        newId = MappingData.validateNewChannel(newId);
        if (newId == null) return null;

        // Default channels that should not be modifiable
        switch (newId) {
            case "minecraft:trader_list":
                return "MC|TrList";
            case "minecraft:book_open":
                return "MC|BOpen";
            case "minecraft:debug/paths":
                return "MC|DebugPath";
            case "minecraft:debug/neighbors_update":
                return "MC|DebugNeighborsUpdate";
            case "minecraft:register":
                return "REGISTER";
            case "minecraft:unregister":
                return "UNREGISTER";
            case "minecraft:brand":
                return "MC|Brand";
            case "bungeecord:main":
                return "BungeeCord";
            default:
                String mappedChannel = Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().inverse().get(newId);
                if (mappedChannel != null) return mappedChannel;
                return newId.length() > 20 ? newId.substring(0, 20) : newId;
        }
    }

    public static boolean isDamageable(int id) {
        return id >= 256 && id <= 259 // iron shovel, pickaxe, axe, flint and steel
                || id == 261 // bow
                || id >= 267 && id <= 279 // iron sword, wooden+stone+diamond swords, shovels, pickaxes, axes
                || id >= 283 && id <= 286 // gold sword, shovel, pickaxe, axe
                || id >= 290 && id <= 294 // hoes
                || id >= 298 && id <= 317 // armors
                || id == 346 // fishing rod
                || id == 359 // shears
                || id == 398 // carrot on a stick
                || id == 442 // shield
                || id == 443; // elytra
    }
}