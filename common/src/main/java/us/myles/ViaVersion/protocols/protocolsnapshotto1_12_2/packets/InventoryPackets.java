package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets;

import com.github.steveice10.opennbt.tag.builtin.*;
import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.ProtocolSnapshotTo1_12_2;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data.SoundSource;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data.SpawnEggRewriter;

import java.util.Map;

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
                            wrapper.setId(0x4B);

                            byte flags = 0;
                            wrapper.write(Type.BYTE, flags); // Placeholder
                            if (!originalSource.isEmpty()) {
                                flags |= 1;
                                Optional<SoundSource> finalSource = SoundSource.findBySource(originalSource);
                                if (!finalSource.isPresent()) {
                                    System.out.println("Could not handle unknown sound source " + originalSource + " falling back to default: master");
                                    finalSource = Optional.of(SoundSource.MASTER);
                                }

                                wrapper.write(Type.VAR_INT, finalSource.get().getId());
                            }
                            if (!originalSound.isEmpty()) {
                                flags |= 2;
                                wrapper.write(Type.STRING, originalSound);
                            }

                            wrapper.set(Type.BYTE, 0, flags); // Update flags
                        }
                        if (channel.equalsIgnoreCase("MC|TrList")) {
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                // Input Item
                                Item input = wrapper.read(Type.ITEM);
                                toClient(input);
                                wrapper.write(Type.ITEM, input);
                                // Output Item
                                Item output = wrapper.read(Type.ITEM);
                                toClient(output);
                                wrapper.write(Type.ITEM, output);

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    // Second Item
                                    Item second = wrapper.read(Type.ITEM);
                                    toClient(second);
                                    wrapper.write(Type.ITEM, second);
                                }

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });

        // Entity Equipment Packet
        protocol.registerOutgoing(State.PLAY, 0x3F, 0x41, new PacketRemapper() {
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
        protocol.registerIncoming(State.PLAY, 0x07, 0x07, new PacketRemapper() {
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

        // Creative Inventory Action
        protocol.registerIncoming(State.PLAY, 0x1B, 0x1B, new PacketRemapper() {
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
    public static void toClient(Item item) {
        if (item == null) return;

        // create tag
        CompoundTag tag = item.getTag();
        if (tag == null) {
            item.setTag(tag = new CompoundTag("tag"));
        }

        // Save original id
        int originalId = (item.getId() << 16 | item.getData() & 0xFFFF);
        tag.put(new IntTag(NBT_TAG_NAME, originalId));

        // NBT changes
        if (isDamageable(item.getId())) {
            tag.put(new IntTag("Damage", item.getData()));
        }

        if (item.getId() == 358) { // map
            tag.put(new IntTag("map", item.getData()));
        }

        if (item.getId() == 442) { // shield
            if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                if (blockEntityTag.get("Base") instanceof IntTag) {
                    IntTag base = blockEntityTag.get("Base");
                    base.setValue(15 - base.getValue()); // invert color id
                }
            }
        }

        // Display Name now uses JSON
        if (tag.get("display") instanceof CompoundTag) {
            if (((CompoundTag) tag.get("display")).get("Name") instanceof StringTag) {
                StringTag name = ((CompoundTag) tag.get("display")).get("Name");
                name.setValue(
                        ProtocolSnapshotTo1_12_2.legacyTextToJson(
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
                    enchantmentEntry.put(new StringTag("id",
                            MappingData.oldEnchantmentsIds.get(
                                    (Short) ((CompoundTag) enchEntry).get("id").getValue()
                            )
                    ));
                    enchantmentEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchEntry).get("lvl").getValue()));
                    enchantments.add(enchantmentEntry);
                }
            }
            tag.remove("ench");
            tag.put(enchantments);
        }

        int rawId = (item.getId() << 4 | item.getData() & 0xF);

        // Handle SpawnEggs
        if (item.getId() == 383) {
            if (tag.get("EntityTag") instanceof CompoundTag) {
                CompoundTag entityTag = tag.get("EntityTag");
                if (entityTag.get("id") instanceof StringTag) {
                    StringTag identifier = entityTag.get("id");
                    rawId = SpawnEggRewriter.getSpawnEggId(identifier.getValue());
                } else {
                    // Fallback to bat
                    rawId = 25100288;
                }
            } else {
                // Fallback to bat
                rawId = 25100288;
            }
        }

        if (!MappingData.oldToNewItems.containsKey(rawId)) {
            if (MappingData.oldToNewItems.containsKey(rawId & ~0xF)) {
                rawId &= ~0xF; // Remove data
            } else {
                System.out.println("FAILED TO GET 1.13 ITEM FOR " + item.getId()); // TODO: Make this nicer etc, perhaps fix issues with mapping :T
                rawId = 16; // Stone
            }
        }

        item.setId(MappingData.oldToNewItems.get(rawId).shortValue());
        item.setData((short) 0);
    }

    // TODO cleanup / smarter rewrite system
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
            Integer oldId = MappingData.newToOldItems.get((int) item.getId());
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
            System.out.println("FAILED TO GET 1.12 ITEM FOR " + item.getId());
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

            if (item.getId() == 442) { // shield
                if (tag.get("BlockEntityTag") instanceof CompoundTag) {
                    CompoundTag blockEntityTag = tag.get("BlockEntityTag");
                    if (blockEntityTag.get("Base") instanceof IntTag) {
                        IntTag base = blockEntityTag.get("Base");
                        base.setValue(15 - base.getValue()); // invert color id
                    }
                }
            }

            // Display Name now uses JSON
            if (tag.get("display") instanceof CompoundTag) {
                if (((CompoundTag) tag.get("display")).get("Name") instanceof StringTag) {
                    StringTag name = ((CompoundTag) tag.get("display")).get("Name");
                    name.setValue(
                            ProtocolSnapshotTo1_12_2.jsonTextToLegacy(
                                    name.getValue()
                            )
                    );
                }
            }

            // ench is now Enchantments and now uses identifiers
            if (tag.get("Enchantments") instanceof ListTag) {
                ListTag enchantments = tag.get("Enchantments");
                ListTag ench = new ListTag("ench", CompoundTag.class);
                for (Tag enchantmentEntry : enchantments) {
                    if (enchantmentEntry instanceof CompoundTag) {
                        CompoundTag enchEntry = new CompoundTag("");
                        enchEntry.put(
                                new ShortTag(
                                        "id",
                                        MappingData.oldEnchantmentsIds.inverse().get(
                                                (String) ((CompoundTag) enchantmentEntry).get("id").getValue()
                                        )
                                )
                        );
                        enchEntry.put(new ShortTag("lvl", (Short) ((CompoundTag) enchantmentEntry).get("lvl").getValue()));
                        ench.add(enchEntry);
                    }
                }
                tag.remove("Enchantment");
                tag.put(ench);
            }
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
