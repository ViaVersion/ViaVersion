package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SoundSource;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InventoryPackets {
    private static final String NBT_TAG_NAME = "ViaVersion|" + Protocol1_13To1_12_2.class.getSimpleName();

    public static void register(Protocol1_13To1_12_2 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        protocol.registerOutgoing(ClientboundPackets1_12_1.SET_SLOT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Slot Value

                handler(itemRewriter.itemToClientHandler(Type.FLAT_ITEM));
            }
        });
        protocol.registerOutgoing(ClientboundPackets1_12_1.WINDOW_ITEMS, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY, Type.FLAT_ITEM_ARRAY); // 1 - Window Values

                handler(itemRewriter.itemArrayHandler(Type.FLAT_ITEM_ARRAY));
            }
        });
        protocol.registerOutgoing(ClientboundPackets1_12_1.WINDOW_PROPERTY, new PacketRemapper() {
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
        protocol.registerOutgoing(ClientboundPackets1_12_1.PLUGIN_MESSAGE, new PacketRemapper() {
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
                                InventoryPackets.toClient(input);
                                wrapper.write(Type.FLAT_ITEM, input);
                                // Output Item
                                Item output = wrapper.read(Type.ITEM);
                                InventoryPackets.toClient(output);
                                wrapper.write(Type.FLAT_ITEM, output);

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    Item second = wrapper.read(Type.ITEM);
                                    InventoryPackets.toClient(second);
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

        protocol.registerOutgoing(ClientboundPackets1_12_1.ENTITY_EQUIPMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Item

                handler(itemRewriter.itemToClientHandler(Type.FLAT_ITEM));
            }
        });


        protocol.registerIncoming(ServerboundPackets1_13.CLICK_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(Type.FLAT_ITEM, Type.ITEM); // 5 - Clicked Item

                handler(itemRewriter.itemToServerHandler(Type.ITEM));
            }
        });

        protocol.registerIncoming(ServerboundPackets1_13.PLUGIN_MESSAGE, new PacketRemapper() {
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

        protocol.registerIncoming(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot
                map(Type.FLAT_ITEM, Type.ITEM); // 1 - Clicked Item

                handler(itemRewriter.itemToServerHandler(Type.ITEM));
            }
        });
    }

    // TODO CLEANUP / SMARTER REWRITE SYSTEM
    // TODO Rewrite identifiers
    public static void toClient(Item item) {
        if (item == null) return;
        CompoundTag tag = item.getTag();

        // Save original id
        int originalId = (item.getIdentifier() << 16 | item.getData() & 0xFFFF);

        int rawId = (item.getIdentifier() << 4 | item.getData() & 0xF);

        // NBT Additions
        if (isDamageable(item.getIdentifier())) {
            if (tag == null) item.setTag(tag = new CompoundTag("tag"));
            tag.put(new IntTag("Damage", item.getData()));
        }
        if (item.getIdentifier() == 358) { // map
            if (tag == null) item.setTag(tag = new CompoundTag("tag"));
            tag.put(new IntTag("map", item.getData()));
        }

        // NBT Changes
        if (tag != null) {
            // Invert banner/shield color id
            boolean banner = item.getIdentifier() == 425;
            if (banner || item.getIdentifier() == 442) {
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
                        // Set banner item id according to nbt
                        if (banner) {
                            rawId = 6800 + base.getValue();
                        }

                        base.setValue(15 - base.getValue());
                    }
                    if (blockEntityTag.get("Patterns") instanceof ListTag) {
                        for (Tag pattern : (ListTag) blockEntityTag.get("Patterns")) {
                            if (pattern instanceof CompoundTag) {
                                IntTag c = ((CompoundTag) pattern).get("Color");
                                c.setValue(15 - c.getValue()); // Invert color id
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
                    display.put(new StringTag(NBT_TAG_NAME + "|Name", name.getValue()));
                    name.setValue(ChatRewriter.legacyTextToJsonString(name.getValue(), true));
                }
            }
            // ench is now Enchantments and now uses identifiers
            if (tag.get("ench") instanceof ListTag) {
                ListTag ench = tag.get("ench");
                ListTag enchantments = new ListTag("Enchantments", CompoundTag.class);
                for (Tag enchEntry : ench) {
                    if (enchEntry instanceof CompoundTag) {
                        CompoundTag enchantmentEntry = new CompoundTag("");
                        short oldId = ((Number) ((CompoundTag) enchEntry).get("id").getValue()).shortValue();
                        String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                        if (newId == null) {
                            newId = "viaversion:legacy/" + oldId;
                        }
                        enchantmentEntry.put(new StringTag("id", newId));
                        enchantmentEntry.put(new ShortTag("lvl", ((Number) ((CompoundTag) enchEntry).get("lvl").getValue()).shortValue()));
                        enchantments.add(enchantmentEntry);
                    }
                }
                tag.remove("ench");
                tag.put(enchantments);
            }
            if (tag.get("StoredEnchantments") instanceof ListTag) {
                ListTag storedEnch = tag.get("StoredEnchantments");
                ListTag newStoredEnch = new ListTag("StoredEnchantments", CompoundTag.class);
                for (Tag enchEntry : storedEnch) {
                    if (enchEntry instanceof CompoundTag) {
                        CompoundTag enchantmentEntry = new CompoundTag("");
                        short oldId = ((Number) ((CompoundTag) enchEntry).get("id").getValue()).shortValue();
                        String newId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                        if (newId == null) {
                            newId = "viaversion:legacy/" + oldId;
                        }
                        enchantmentEntry.put(new StringTag("id",
                                newId
                        ));
                        enchantmentEntry.put(new ShortTag("lvl", ((Number) ((CompoundTag) enchEntry).get("lvl").getValue()).shortValue()));
                        newStoredEnch.add(enchantmentEntry);
                    }
                }
                tag.remove("StoredEnchantments");
                tag.put(newStoredEnch);
            }
            if (tag.get("CanPlaceOn") instanceof ListTag) {
                ListTag old = tag.get("CanPlaceOn");
                ListTag newCanPlaceOn = new ListTag("CanPlaceOn", StringTag.class);
                tag.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|CanPlaceOn", ConverterRegistry.convertToValue(old))); // There will be data losing
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
                            newCanPlaceOn.add(new StringTag("", newValue));
                        }
                    } else {
                        newCanPlaceOn.add(new StringTag("", oldId.toLowerCase(Locale.ROOT)));
                    }
                }
                tag.put(newCanPlaceOn);
            }
            if (tag.get("CanDestroy") instanceof ListTag) {
                ListTag old = tag.get("CanDestroy");
                ListTag newCanDestroy = new ListTag("CanDestroy", StringTag.class);
                tag.put(ConverterRegistry.convertToTag(NBT_TAG_NAME + "|CanDestroy", ConverterRegistry.convertToValue(old))); // There will be data losing
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
                            newCanDestroy.add(new StringTag("", newValue));
                        }
                    } else {
                        newCanDestroy.add(new StringTag("", oldId.toLowerCase(Locale.ROOT)));
                    }
                }
                tag.put(newCanDestroy);
            }
            // Handle SpawnEggs
            if (item.getIdentifier() == 383) {
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
            if (!isDamageable(item.getIdentifier()) && item.getIdentifier() != 358) { // Map
                if (tag == null) item.setTag(tag = new CompoundTag("tag"));
                tag.put(new IntTag(NBT_TAG_NAME, originalId)); // Data will be lost, saving original id
            }
            if (item.getIdentifier() == 31 && item.getData() == 0) { // Shrub was removed
                rawId = 32 << 4; // Dead Bush
            } else if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().containsKey(rawId & ~0xF)) {
                rawId &= ~0xF; // Remove data
            } else {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.getIdentifier());
                }
                rawId = 16; // Stone
            }
        }

        item.setIdentifier(Protocol1_13To1_12_2.MAPPINGS.getItemMappings().get(rawId));
        item.setData((short) 0);
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

    public static void toServer(Item item) {
        if (item == null) return;

        Integer rawId = null;
        boolean gotRawIdFromTag = false;

        CompoundTag tag = item.getTag();

        // Use tag to get original ID and data
        if (tag != null) {
            // Check for valid tag
            if (tag.get(NBT_TAG_NAME) instanceof IntTag) {
                rawId = (Integer) tag.get(NBT_TAG_NAME).getValue();
                // Remove the tag
                tag.remove(NBT_TAG_NAME);
                gotRawIdFromTag = true;
            }
        }

        if (rawId == null) {
            int oldId = Protocol1_13To1_12_2.MAPPINGS.getItemMappings().inverse().get(item.getIdentifier());
            if (oldId != -1) {
                // Handle spawn eggs
                Optional<String> eggEntityId = SpawnEggRewriter.getEntityId(oldId);
                if (eggEntityId.isPresent()) {
                    rawId = 383 << 16;
                    if (tag == null)
                        item.setTag(tag = new CompoundTag("tag"));
                    if (!tag.contains("EntityTag")) {
                        CompoundTag entityTag = new CompoundTag("EntityTag");
                        entityTag.put(new StringTag("id", eggEntityId.get()));
                        tag.put(entityTag);
                    }
                } else {
                    rawId = (oldId >> 4) << 16 | oldId & 0xF;
                }
            }
        }

        if (rawId == null) {
            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Failed to get 1.12 item for " + item.getIdentifier());
            }
            rawId = 0x10000; // Stone
        }

        item.setIdentifier((short) (rawId >> 16));
        item.setData((short) (rawId & 0xFFFF));

        // NBT changes
        if (tag != null) {
            if (isDamageable(item.getIdentifier())) {
                if (tag.get("Damage") instanceof IntTag) {
                    if (!gotRawIdFromTag) {
                        item.setData((short) (int) tag.get("Damage").getValue());
                    }
                    tag.remove("Damage");
                }
            }

            if (item.getIdentifier() == 358) { // map
                if (tag.get("map") instanceof IntTag) {
                    if (!gotRawIdFromTag) {
                        item.setData((short) (int) tag.get("map").getValue());
                    }
                    tag.remove("map");
                }
            }

            if (item.getIdentifier() == 442 || item.getIdentifier() == 425) { // shield / banner
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
                        base.setValue(15 - base.getValue()); // invert color id
                    }
                    if (blockEntityTag.get("Patterns") instanceof ListTag) {
                        for (Tag pattern : (ListTag) blockEntityTag.get("Patterns")) {
                            if (pattern instanceof CompoundTag) {
                                IntTag c = ((CompoundTag) pattern).get("Color");
                                c.setValue(15 - c.getValue()); // Invert color id
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
                ListTag ench = new ListTag("ench", CompoundTag.class);
                for (Tag enchantmentEntry : enchantments) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag("");
                        String newId = (String) ((CompoundTag) enchantmentEntry).get("id").getValue();
                        Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        if (oldId != null) {
                            enchEntry.put(new ShortTag("id", oldId));
                            enchEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchantmentEntry).get("lvl").getValue()));
                            ench.add(enchEntry);
                        }
                    }
                }
                tag.remove("Enchantments");
                tag.put(ench);
            }
            if (tag.get("StoredEnchantments") instanceof ListTag) {
                ListTag storedEnch = tag.get("StoredEnchantments");
                ListTag newStoredEnch = new ListTag("StoredEnchantments", CompoundTag.class);
                for (Tag enchantmentEntry : storedEnch) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag("");
                        String newId = (String) ((CompoundTag) enchantmentEntry).get("id").getValue();
                        Short oldId = Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        if (oldId != null) {
                            enchEntry.put(new ShortTag("id", oldId));
                            enchEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchantmentEntry).get("lvl").getValue()));
                            newStoredEnch.add(enchEntry);
                        }
                    }
                }
                tag.remove("StoredEnchantments");
                tag.put(newStoredEnch);
            }
            if (tag.get(NBT_TAG_NAME + "|CanPlaceOn") instanceof ListTag) {
                tag.put(ConverterRegistry.convertToTag(
                        "CanPlaceOn",
                        ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanPlaceOn"))
                ));
                tag.remove(NBT_TAG_NAME + "|CanPlaceOn");
            } else if (tag.get("CanPlaceOn") instanceof ListTag) {
                ListTag old = tag.get("CanPlaceOn");
                ListTag newCanPlaceOn = new ListTag("CanPlaceOn", StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? ((String) value).replace("minecraft:", "")
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag("", newValue));
                        }
                    } else {
                        newCanPlaceOn.add(oldTag);
                    }
                }
                tag.put(newCanPlaceOn);
            }
            if (tag.get(NBT_TAG_NAME + "|CanDestroy") instanceof ListTag) {
                tag.put(ConverterRegistry.convertToTag(
                        "CanDestroy",
                        ConverterRegistry.convertToValue(tag.get(NBT_TAG_NAME + "|CanDestroy"))
                ));
                tag.remove(NBT_TAG_NAME + "|CanDestroy");
            } else if (tag.get("CanDestroy") instanceof ListTag) {
                ListTag old = tag.get("CanDestroy");
                ListTag newCanDestroy = new ListTag("CanDestroy", StringTag.class);
                for (Tag oldTag : old) {
                    Object value = oldTag.getValue();
                    String[] newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String
                            ? ((String) value).replace("minecraft:", "")
                            : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag("", newValue));
                        }
                    } else {
                        newCanDestroy.add(oldTag);
                    }
                }
                tag.put(newCanDestroy);
            }
        }
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
