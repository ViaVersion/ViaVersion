package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.tag.builtin.*;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SoundSource;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InventoryPackets {
    private static String NBT_TAG_NAME;

    public static void register(Protocol protocol) {
        NBT_TAG_NAME = "ViaVersion|" + protocol.getClass().getSimpleName();

        /*
            Outgoing packets
         */

        // Set slot packet
        protocol.registerOutgoing(State.PLAY, 0x16, 0x17, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Slot Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.FLAT_ITEM, 0);
                        toClient(stack);
                    }
                });
            }
        });

        // Window items packet
        protocol.registerOutgoing(State.PLAY, 0x14, 0x15, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY, Type.FLAT_ITEM_ARRAY); // 1 - Window Values

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item[] stacks = wrapper.get(Type.FLAT_ITEM_ARRAY, 0);
                        for (Item stack : stacks)
                            toClient(stack);
                    }
                });
            }
        });

        // Window property
        protocol.registerOutgoing(State.PLAY, 0x15, 0x16, new PacketRemapper() {
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
                            wrapper.set(Type.SHORT, 1, (short) MappingData.enchantmentMappings.getNewEnchantment(
                                    wrapper.get(Type.SHORT, 1)
                            ));
                        }
                    }
                });
            }
        });

        // Plugin message Packet -> Trading
        protocol.registerOutgoing(State.PLAY, 0x18, 0x19, new PacketRemapper() {
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
                                    if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
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
                            channel = getNewPluginChannelId(channel);
                            if (channel == null) {
                                wrapper.cancel();
                                return;
                            } else if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                                String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                                List<String> rewrittenChannels = new ArrayList<>();
                                for (int i = 0; i < channels.length; i++) {
                                    String rewritten = getNewPluginChannelId(channels[i]);
                                    if (rewritten != null) {
                                        rewrittenChannels.add(rewritten);
                                    } else if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                                        Via.getPlatform().getLogger().warning("Ignoring plugin channel in REGISTER: " + channels[i]);
                                    }
                                }
                                wrapper.write(Type.REMAINING_BYTES, Joiner.on('\0').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        wrapper.set(Type.STRING, 0, channel);
                    }
                    // TODO Fix trading GUI
                });
            }
        });

        // Entity Equipment Packet
        protocol.registerOutgoing(State.PLAY, 0x3F, 0x42, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM, Type.FLAT_ITEM); // 2 - Item

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.FLAT_ITEM, 0);
                        toClient(stack);
                    }
                });
            }
        });


        /*
            Incoming packets
         */

        // Click window packet
        protocol.registerIncoming(State.PLAY, 0x07, 0x08, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.UNSIGNED_BYTE); // 0 - Window ID
                        map(Type.SHORT); // 1 - Slot
                        map(Type.BYTE); // 2 - Button
                        map(Type.SHORT); // 3 - Action number
                        map(Type.VAR_INT); // 4 - Mode
                        map(Type.FLAT_ITEM, Type.ITEM); // 5 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                toServer(item);
                            }
                        });
                    }
                }
        );

        // Plugin message
        protocol.registerIncoming(State.PLAY, 0x09, 0x0A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Channel
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.get(Type.STRING, 0);
                        channel = getOldPluginChannelId(channel);
                        if (channel == null) {
                            wrapper.cancel();
                            return;
                        } else if (channel.equals("REGISTER") || channel.equals("UNREGISTER")) {
                            String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                            List<String> rewrittenChannels = new ArrayList<>();
                            for (int i = 0; i < channels.length; i++) {
                                String rewritten = getOldPluginChannelId(channels[i]);
                                if (rewritten != null) {
                                    rewrittenChannels.add(rewritten);
                                } else if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                                    Via.getPlatform().getLogger().warning("Ignoring plugin channel in REGISTER: " + channels[i]);
                                }
                            }
                            wrapper.write(Type.REMAINING_BYTES, Joiner.on('\0').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                        }
                        wrapper.set(Type.STRING, 0, channel);
                    }
                });
            }
        });

        // Creative Inventory Action
        protocol.registerIncoming(State.PLAY, 0x1B, 0x24, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.SHORT); // 0 - Slot
                        map(Type.FLAT_ITEM, Type.ITEM); // 1 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                toServer(item);
                            }
                        });
                    }
                }
        );
    }

    // TODO CLEANUP / SMARTER REWRITE SYSTEM
    // TODO Rewrite identifiers
    public static void toClient(Item item) {
        if (item == null) return;
        CompoundTag tag = item.getTag();

        // Save original id
        int originalId = (item.getId() << 16 | item.getData() & 0xFFFF);

        int rawId = (item.getId() << 4 | item.getData() & 0xF);

        // NBT Additions
        if (isDamageable(item.getId())) {
            if (tag == null) item.setTag(tag = new CompoundTag("tag"));
            tag.put(new IntTag("Damage", item.getData()));
        }
        if (item.getId() == 358) { // map
            if (tag == null) item.setTag(tag = new CompoundTag("tag"));
            tag.put(new IntTag("map", item.getData()));
        }

        // NBT Changes
        if (tag != null) {
            // Invert shield color id
            if (item.getId() == 442 || item.getId() == 425) {
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
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
                    name.setValue(
                            ChatRewriter.legacyTextToJson(
                                    name.getValue()
                            )
                    );
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
                        String newId = MappingData.oldEnchantmentsIds.get(oldId);
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
                        String newId = MappingData.oldEnchantmentsIds.get(oldId);
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
            // Handle SpawnEggs
            if (item.getId() == 383) {
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

        if (!MappingData.oldToNewItems.containsKey(rawId)) {
            if (!isDamageable(item.getId()) && item.getId() != 358) { // Map
                if (tag == null) item.setTag(tag = new CompoundTag("tag"));
                tag.put(new IntTag(NBT_TAG_NAME, originalId)); // Data will be lost, saving original id
            }
            if (item.getId() == 31 && item.getData() == 0) { // Shrub was removed
                rawId = 32 << 4; // Dead Bush
            } else if (MappingData.oldToNewItems.containsKey(rawId & ~0xF)) {
                rawId &= ~0xF; // Remove data
            } else {
                if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.getId());
                }
                rawId = 16; // Stone
            }
        }

        item.setId(MappingData.oldToNewItems.get(rawId).shortValue());
        item.setData((short) 0);
    }

    public static String getNewPluginChannelId(String old) {
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
            case "WDL|INIT":
                return "wdl:init";
            case "WDL|CONTROL":
                return "wdl:init";
            case "WDL|REQUEST":
                return "wdl:request";
            default:
                return old.matches("[0-9a-z_-]+:[0-9a-z_/.-]+") // Identifier regex
                        ? old
                        : "viaversion:legacy/" + BaseEncoding.base32().lowerCase().withPadChar('-').encode(
                        old.getBytes(StandardCharsets.UTF_8));
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
            Integer oldId = MappingData.oldToNewItems.inverse().get((int) item.getId());
            if (oldId != null) {
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
            if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Failed to get 1.12 item for " + item.getId());
            }
            rawId = 0x10000; // Stone
        }

        item.setId((short) (rawId >> 16));
        item.setData((short) (rawId & 0xFFFF));

        // NBT changes
        if (tag != null) {
            if (isDamageable(item.getId())) {
                if (tag.get("Damage") instanceof IntTag) {
                    if (!gotRawIdFromTag)
                        item.setData((short) (int) tag.get("Damage").getValue());
                    tag.remove("Damage");
                }
            }

            if (item.getId() == 358) { // map
                if (tag.get("map") instanceof IntTag) {
                    if (!gotRawIdFromTag)
                        item.setData((short) (int) tag.get("map").getValue());
                    tag.remove("map");
                }
            }

            if (item.getId() == 442 || item.getId() == 425) { // shield / banner
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
                if (((CompoundTag) tag.get("display")).get("Name") instanceof StringTag) {
                    StringTag name = display.get("Name");
                    StringTag via = display.get(NBT_TAG_NAME + "|Name");
                    name.setValue(
                            via != null ? via.getValue() : ChatRewriter.jsonTextToLegacy(
                                    name.getValue()
                            )
                    );
                    display.remove(NBT_TAG_NAME + "|Name");
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
                        Short oldId = MappingData.oldEnchantmentsIds.inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        enchEntry.put(
                                new ShortTag(
                                        "id",
                                        oldId
                                )
                        );
                        enchEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchantmentEntry).get("lvl").getValue()));
                        ench.add(enchEntry);
                    }
                }
                tag.remove("Enchantment");
                tag.put(ench);
            }
            if (tag.get("StoredEnchantments") instanceof ListTag) {
                ListTag storedEnch = tag.get("StoredEnchantments");
                ListTag newStoredEnch = new ListTag("StoredEnchantments", CompoundTag.class);
                for (Tag enchantmentEntry : storedEnch) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag("");
                        String newId = (String) ((CompoundTag) enchantmentEntry).get("id").getValue();
                        Short oldId = MappingData.oldEnchantmentsIds.inverse().get(newId);
                        if (oldId == null && newId.startsWith("viaversion:legacy/")) {
                            oldId = Short.valueOf(newId.substring(18));
                        }
                        enchEntry.put(
                                new ShortTag(
                                        "id",
                                        oldId
                                )
                        );
                        enchEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchantmentEntry).get("lvl").getValue()));
                        newStoredEnch.add(enchEntry);
                    }
                }
                tag.remove("StoredEnchantments");
                tag.put(newStoredEnch);
            }
        }
    }

    public static String getOldPluginChannelId(String newId) {
        switch (newId) {
            case "minecraft:register":
                return "REGISTER";
            case "minecraft:unregister":
                return "UNREGISTER";
            case "minecraft:brand":
                return "MC|Brand";
            case "bungeecord:main":
                return "BungeeCord";
            case "wdl:init":
                return "WDL|INIT";
            case "wdl:control":
                return "WDL|CONTROL";
            case "wdl:request":
                return "WDL|REQUEST";
            default:
                return newId.startsWith("viaversion:legacy/") // Our format :)
                        ? new String(BaseEncoding.base32().lowerCase().withPadChar('-').decode(
                        newId.substring(18)), StandardCharsets.UTF_8)
                        : newId.startsWith("legacy:")
                        ? newId.substring(7) // Rewrite BungeeCord's format. It will only prevent kicks, plugins will still be broken because of case-sensitivity *plays sad violin*
                        : newId.startsWith("bungeecord:legacy/")
                        ? newId.substring(18)
                        : newId;
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
