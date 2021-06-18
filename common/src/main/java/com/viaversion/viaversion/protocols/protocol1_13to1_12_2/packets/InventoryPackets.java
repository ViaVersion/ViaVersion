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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SoundSource;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InventoryPackets extends ItemRewriter<Protocol1_13To1_12_2> {
    private static final String NBT_TAG_NAME = "ViaVersion|" + Protocol1_13To1_12_2.class.getSimpleName();

    public InventoryPackets(Protocol1_13To1_12_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_12_1.SET_SLOT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Slot Value

                handler(itemToClientHandler(Type.FLAT_ITEM));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.WINDOW_ITEMS, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY, Type.FLAT_ITEM_ARRAY); // 1 - Window Values

                handler(itemArrayHandler(Type.FLAT_ITEM_ARRAY));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.WINDOW_PROPERTY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.SHORT); // Property
                map(Type.SHORT); // Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short property = wrapper.get(Type.SHORT, 0);
                        if (property >= 4 && property <= 6) { // Enchantment id
                            wrapper.set(Type.SHORT, 1, (short) protocol.getMappingData().getEnchantmentMappings().getNewId(wrapper.get(Type.SHORT, 1)));
                        }
                    }
                });
            }
        });

        // Plugin message Packet -> Trading
        protocol.registerClientbound(ClientboundPackets1_12_1.PLUGIN_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.get(Type.STRING, 0);
                        // Handle stopsound change TODO change location of this remap to other class?
                        if (channel.equalsIgnoreCase("MC|StopSound")) {
                            String originalSource = wrapper.read(Type.STRING);
                            String originalSound = wrapper.read(Type.STRING);

                            // Reset the packet
                            wrapper.clearPacket();
                            wrapper.setId(0x4C);

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
                        } else if (channel.equalsIgnoreCase("MC|TrList")) {
                            channel = "minecraft:trader_list";
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                // Input Item
                                Item input = wrapper.read(Type.ITEM);
                                handleItemToClient(input);
                                wrapper.write(Type.FLAT_ITEM, input);
                                // Output Item
                                Item output = wrapper.read(Type.ITEM);
                                handleItemToClient(output);
                                wrapper.write(Type.FLAT_ITEM, output);

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    Item second = wrapper.read(Type.ITEM);
                                    handleItemToClient(second);
                                    wrapper.write(Type.FLAT_ITEM, second);
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
                                    Via.getPlatform().getLogger().warning("Ignoring outgoing plugin message with channel: " + old);
                                }
                                wrapper.cancel();
                                return;
                            } else if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                                String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                                List<String> rewrittenChannels = new ArrayList<>();
                                for (int i = 0; i < channels.length; i++) {
                                    String rewritten = getNewPluginChannelId(channels[i]);
                                    if (rewritten != null) {
                                        rewrittenChannels.add(rewritten);
                                    } else if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                        Via.getPlatform().getLogger().warning("Ignoring plugin channel in outgoing REGISTER: " + channels[i]);
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
                    }
                    // TODO Fix trading GUI
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.ENTITY_EQUIPMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Item

                handler(itemToClientHandler(Type.FLAT_ITEM));
            }
        });


        protocol.registerServerbound(ServerboundPackets1_13.CLICK_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(Type.FLAT_ITEM, Type.ITEM); // 5 - Clicked Item

                handler(itemToServerHandler(Type.ITEM));
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.PLUGIN_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Channel
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.get(Type.STRING, 0);
                        String old = channel;
                        channel = getOldPluginChannelId(channel);
                        if (channel == null) {
                            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                Via.getPlatform().getLogger().warning("Ignoring incoming plugin message with channel: " + old);
                            }
                            wrapper.cancel();
                            return;
                        } else if (channel.equals("REGISTER") || channel.equals("UNREGISTER")) {
                            String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                            List<String> rewrittenChannels = new ArrayList<>();
                            for (int i = 0; i < channels.length; i++) {
                                String rewritten = getOldPluginChannelId(channels[i]);
                                if (rewritten != null) {
                                    rewrittenChannels.add(rewritten);
                                } else if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                    Via.getPlatform().getLogger().warning("Ignoring plugin channel in incoming REGISTER: " + channels[i]);
                                }
                            }
                            wrapper.write(Type.REMAINING_BYTES, Joiner.on('\0').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                        }
                        wrapper.set(Type.STRING, 0, channel);
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot
                map(Type.FLAT_ITEM, Type.ITEM); // 1 - Clicked Item

                handler(itemToServerHandler(Type.ITEM));
            }
        });
    }

    @Override
    public Item handleItemToClient(Item item) {
        if (item == null) return null;
        CompoundTag tag = item.tag();

        // Save original id
        int originalId = (item.identifier() << 16 | item.data() & 0xFFFF);

        int rawId = (item.identifier() << 4 | item.data() & 0xF);

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
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
                        // Set banner item id according to nbt
                        if (banner) {
                            rawId = 6800 + base.asInt();
                        }

                        base.setValue(15 - base.asInt());
                    }
                    if (blockEntityTag.get("Patterns") instanceof ListTag) {
                        for (Tag pattern : (ListTag) blockEntityTag.get("Patterns")) {
                            if (pattern instanceof CompoundTag) {
                                IntTag c = ((CompoundTag) pattern).get("Color");
                                c.setValue(15 - c.asInt()); // Invert color id
                            }
                        }
                    }
                }
            }
            // Display Name now uses JSON
            if (tag.get("display") instanceof CompoundTag) {
                CompoundTag display = tag.get("display");
                if (display.get("Name") instanceof StringTag) {
                    StringTag name = display.get("Name");
                    display.put(NBT_TAG_NAME + "|Name", new StringTag(name.getValue()));
                    name.setValue(ChatRewriter.legacyTextToJsonString(name.getValue(), true));
                }
            }
            // ench is now Enchantments and now uses identifiers
            if (tag.get("ench") instanceof ListTag) {
                ListTag ench = tag.get("ench");
                ListTag enchantments = new ListTag(CompoundTag.class);
                for (Tag enchEntry : ench) {
                    NumberTag idTag;
                    if (enchEntry instanceof CompoundTag && (idTag = ((CompoundTag) enchEntry).get("id")) != null) {
                        CompoundTag enchantmentEntry = new CompoundTag();
                        short oldId = idTag.asShort();
                        String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                        if (newId == null) {
                            newId = "viaversion:legacy/" + oldId;
                        }
                        enchantmentEntry.put("id", new StringTag(newId));
                        enchantmentEntry.put("lvl", new ShortTag(((NumberTag) ((CompoundTag) enchEntry).get("lvl")).asShort()));
                        enchantments.add(enchantmentEntry);
                    }
                }
                tag.remove("ench");
                tag.put("Enchantments", enchantments);
            }
            if (tag.get("StoredEnchantments") instanceof ListTag) {
                ListTag storedEnch = tag.get("StoredEnchantments");
                ListTag newStoredEnch = new ListTag(CompoundTag.class);
                for (Tag enchEntry : storedEnch) {
                    if (enchEntry instanceof CompoundTag) {
                        CompoundTag enchantmentEntry = new CompoundTag();
                        short oldId = ((NumberTag) ((CompoundTag) enchEntry).get("id")).asShort();
                        String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                        if (newId == null) {
                            newId = "viaversion:legacy/" + oldId;
                        }
                        enchantmentEntry.put("id", new StringTag(newId));
                        enchantmentEntry.put("lvl", new ShortTag(((NumberTag) ((CompoundTag) enchEntry).get("lvl")).asShort()));
                        newStoredEnch.add(enchantmentEntry);
                    }
                }
                tag.remove("StoredEnchantments");
                tag.put("StoredEnchantments", newStoredEnch);
            }
            if (tag.get("CanPlaceOn") instanceof ListTag) {
                ListTag old = tag.get("CanPlaceOn");
                ListTag newCanPlaceOn = new ListTag(StringTag.class);
                tag.put(NBT_TAG_NAME + "|CanPlaceOn", ConverterRegistry.convertToTag(ConverterRegistry.convertToValue(old))); // There will be data losing
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String oldId = value.toString().replace("minecraft:", "");
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
            if (tag.get("CanDestroy") instanceof ListTag) {
                ListTag old = tag.get("CanDestroy");
                ListTag newCanDestroy = new ListTag(StringTag.class);
                tag.put(NBT_TAG_NAME + "|CanDestroy", ConverterRegistry.convertToTag(ConverterRegistry.convertToValue(old))); // There will be data losing
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String oldId = value.toString().replace("minecraft:", "");
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
                if (tag.get("EntityTag") instanceof CompoundTag) {
                    CompoundTag entityTag = tag.get("EntityTag");
                    if (entityTag.get("id") instanceof StringTag) {
                        StringTag identifier = entityTag.get("id");
                        rawId = SpawnEggRewriter.getSpawnEggId(identifier.getValue());
                        if (rawId == -1) {
                            rawId = 25100288; // Bat fallback
                        } else {
                            entityTag.remove("id");
                            if (entityTag.isEmpty())
                                tag.remove("EntityTag");
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

        if (!Protocol1_13To1_12_2.MAPPINGS.getItemMappings().containsKey(rawId)) {
            if (!isDamageable(item.identifier()) && item.identifier() != 358) { // Map
                if (tag == null) item.setTag(tag = new CompoundTag());
                tag.put(NBT_TAG_NAME, new IntTag(originalId)); // Data will be lost, saving original id
            }
            if (item.identifier() == 31 && item.data() == 0) { // Shrub was removed
                rawId = 32 << 4; // Dead Bush
            } else if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().containsKey(rawId & ~0xF)) {
                rawId &= ~0xF; // Remove data
            } else {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.identifier());
                }
                rawId = 16; // Stone
            }
        }

        item.setIdentifier(Protocol1_13To1_12_2.MAPPINGS.getItemMappings().get(rawId));
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
                return MappingData.isValid1_13Channel(old) ? old : null;
        }
    }

    @Override
    public Item handleItemToServer(Item item) {
        if (item == null) return null;

        Integer rawId = null;
        boolean gotRawIdFromTag = false;

        CompoundTag tag = item.tag();

        // Use tag to get original ID and data
        if (tag != null) {
            // Check for valid tag
            if (tag.get(NBT_TAG_NAME) instanceof IntTag) {
                rawId = ((NumberTag) tag.get(NBT_TAG_NAME)).asInt();
                // Remove the tag
                tag.remove(NBT_TAG_NAME);
                gotRawIdFromTag = true;
            }
        }

        if (rawId == null) {
            int oldId = Protocol1_13To1_12_2.MAPPINGS.getItemMappings().inverse().get(item.identifier());
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
                    rawId = (oldId >> 4) << 16 | oldId & 0xF;
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
                if (tag.get("Damage") instanceof IntTag) {
                    if (!gotRawIdFromTag) {
                        item.setData((short) (int) tag.get("Damage").getValue());
                    }
                    tag.remove("Damage");
                }
            }

            if (item.identifier() == 358) { // map
                if (tag.get("map") instanceof IntTag) {
                    if (!gotRawIdFromTag) {
                        item.setData((short) (int) tag.get("map").getValue());
                    }
                    tag.remove("map");
                }
            }

            if (item.identifier() == 442 || item.identifier() == 425) { // shield / banner
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
                        base.setValue(15 - base.asInt()); // invert color id
                    }
                    if (blockEntityTag.get("Patterns") instanceof ListTag) {
                        for (Tag pattern : (ListTag) blockEntityTag.get("Patterns")) {
                            if (pattern instanceof CompoundTag) {
                                IntTag c = ((CompoundTag) pattern).get("Color");
                                c.setValue(15 - c.asInt()); // Invert color id
                            }
                        }
                    }
                }
            }
            // Display Name now uses JSON
            if (tag.get("display") instanceof CompoundTag) {
                CompoundTag display = tag.get("display");
                if (display.get("Name") instanceof StringTag) {
                    StringTag name = display.get("Name");
                    StringTag via = display.remove(NBT_TAG_NAME + "|Name");
                    name.setValue(via != null ? via.getValue() : ChatRewriter.jsonToLegacyText(name.getValue()));
                }
            }

            // ench is now Enchantments and now uses identifiers
            if (tag.get("Enchantments") instanceof ListTag) {
                ListTag enchantments = tag.get("Enchantments");
                ListTag ench = new ListTag(CompoundTag.class);
                for (Tag enchantmentEntry : enchantments) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag();
                        String newId = (String) ((CompoundTag) enchantmentEntry).get("id").getValue();
                        Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        if (oldId != null) {
                            enchEntry.put("id", new ShortTag(oldId));
                            enchEntry.put("lvl", new ShortTag(((NumberTag) ((CompoundTag) enchantmentEntry).get("lvl")).asShort()));
                            ench.add(enchEntry);
                        }
                    }
                }
                tag.remove("Enchantments");
                tag.put("ench", ench);
            }
            if (tag.get("StoredEnchantments") instanceof ListTag) {
                ListTag storedEnch = tag.get("StoredEnchantments");
                ListTag newStoredEnch = new ListTag(CompoundTag.class);
                for (Tag enchantmentEntry : storedEnch) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag();
                        String newId = (String) ((CompoundTag) enchantmentEntry).get("id").getValue();
                        Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        if (oldId != null) {
                            enchEntry.put("id", new ShortTag(oldId));
                            enchEntry.put("lvl", new ShortTag(((NumberTag) ((CompoundTag) enchantmentEntry).get("lvl")).asShort()));
                            newStoredEnch.add(enchEntry);
                        }
                    }
                }
                tag.remove("StoredEnchantments");
                tag.put("StoredEnchantments", newStoredEnch);
            }
            if (tag.get(NBT_TAG_NAME + "|CanPlaceOn") instanceof ListTag) {
                tag.put("CanPlaceOn", ConverterRegistry.convertToTag(ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanPlaceOn"))));
                tag.remove(NBT_TAG_NAME + "|CanPlaceOn");
            } else if (tag.get("CanPlaceOn") instanceof ListTag) {
                ListTag old = tag.get("CanPlaceOn");
                ListTag newCanPlaceOn = new ListTag(StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? ((String) value).replace("minecraft:", "")
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag(newValue));
                        }
                    } else {
                        newCanPlaceOn.add(oldTag);
                    }
                }
                tag.put("CanPlaceOn", newCanPlaceOn);
            }
            if (tag.get(NBT_TAG_NAME + "|CanDestroy") instanceof ListTag) {
                tag.put("CanDestroy", ConverterRegistry.convertToTag(
                        ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanDestroy"))
                ));
                tag.remove(NBT_TAG_NAME + "|CanDestroy");
            } else if (tag.get("CanDestroy") instanceof ListTag) {
                ListTag old = tag.get("CanDestroy");
                ListTag newCanDestroy = new ListTag(StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? ((String) value).replace("minecraft:", "")
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag(newValue));
                        }
                    } else {
                        newCanDestroy.add(oldTag);
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
