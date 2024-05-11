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
package com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.AttributeMappings;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.metadata.MetadataRewriter1_16To1_15_2;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.InventoryTracker1_16;
import com.viaversion.viaversion.util.Key;
import java.util.Arrays;
import java.util.UUID;

public class EntityPacketRewriter1_16 {

    private static final PacketHandler DIMENSION_HANDLER = wrapper -> {
        WorldIdentifiers map = Via.getConfig().get1_16WorldNamesMap();
        WorldIdentifiers userMap = wrapper.user().get(WorldIdentifiers.class);
        if (userMap != null) {
            map = userMap;
        }
        int dimension = wrapper.read(Types.INT);
        String dimensionName;
        String outputName;
        switch (dimension) {
            case -1:
                dimensionName = "minecraft:the_nether";
                outputName = map.nether();
                break;
            case 0:
                dimensionName = "minecraft:overworld";
                outputName = map.overworld();
                break;
            case 1:
                dimensionName = "minecraft:the_end";
                outputName = map.end();
                break;
            default:
                Via.getPlatform().getLogger().warning("Invalid dimension id: " + dimension);
                dimensionName = "minecraft:overworld";
                outputName = map.overworld();
        }

        wrapper.write(Types.STRING, dimensionName); // dimension
        wrapper.write(Types.STRING, outputName); // world
    };
    public static final CompoundTag DIMENSIONS_TAG = new CompoundTag();
    private static final String[] WORLD_NAMES = {"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};

    static {
        ListTag<CompoundTag> list = new ListTag<>(CompoundTag.class);
        list.add(createOverworldEntry());
        list.add(createOverworldCavesEntry());
        list.add(createNetherEntry());
        list.add(createEndEntry());
        DIMENSIONS_TAG.put("dimension", list);
    }

    private static CompoundTag createOverworldEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("name", new StringTag("minecraft:overworld"));
        tag.put("has_ceiling", new ByteTag((byte) 0));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static CompoundTag createOverworldCavesEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("name", new StringTag("minecraft:overworld_caves"));
        tag.put("has_ceiling", new ByteTag((byte) 1));
        addSharedOverwaldEntries(tag);
        return tag;
    }

    private static void addSharedOverwaldEntries(CompoundTag tag) {
        tag.put("piglin_safe", new ByteTag((byte) 0));
        tag.put("natural", new ByteTag((byte) 1));
        tag.put("ambient_light", new FloatTag(0));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_overworld"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 0));
        tag.put("has_skylight", new ByteTag((byte) 1));
        tag.put("bed_works", new ByteTag((byte) 1));
        tag.put("has_raids", new ByteTag((byte) 1));
        tag.put("logical_height", new IntTag(256));
        tag.put("shrunk", new ByteTag((byte) 0));
        tag.put("ultrawarm", new ByteTag((byte) 0));
    }

    private static CompoundTag createNetherEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("piglin_safe", new ByteTag((byte) 1));
        tag.put("natural", new ByteTag((byte) 0));
        tag.put("ambient_light", new FloatTag(0.1F));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_nether"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 1));
        tag.put("has_skylight", new ByteTag((byte) 0));
        tag.put("bed_works", new ByteTag((byte) 0));
        tag.put("fixed_time", new LongTag(18000));
        tag.put("has_raids", new ByteTag((byte) 0));
        tag.put("name", new StringTag("minecraft:the_nether"));
        tag.put("logical_height", new IntTag(128));
        tag.put("shrunk", new ByteTag((byte) 1));
        tag.put("ultrawarm", new ByteTag((byte) 1));
        tag.put("has_ceiling", new ByteTag((byte) 1));
        return tag;
    }

    private static CompoundTag createEndEntry() {
        CompoundTag tag = new CompoundTag();
        tag.put("piglin_safe", new ByteTag((byte) 0));
        tag.put("natural", new ByteTag((byte) 0));
        tag.put("ambient_light", new FloatTag(0));
        tag.put("infiniburn", new StringTag("minecraft:infiniburn_end"));
        tag.put("respawn_anchor_works", new ByteTag((byte) 0));
        tag.put("has_skylight", new ByteTag((byte) 0));
        tag.put("bed_works", new ByteTag((byte) 0));
        tag.put("fixed_time", new LongTag(6000));
        tag.put("has_raids", new ByteTag((byte) 1));
        tag.put("name", new StringTag("minecraft:the_end"));
        tag.put("logical_height", new IntTag(256));
        tag.put("shrunk", new ByteTag((byte) 0));
        tag.put("ultrawarm", new ByteTag((byte) 0));
        tag.put("has_ceiling", new ByteTag((byte) 0));
        return tag;
    }

    public static void register(Protocol1_15_2To1_16 protocol) {
        MetadataRewriter1_16To1_15_2 metadataRewriter = protocol.get(MetadataRewriter1_16To1_15_2.class);

        // Spawn lightning -> Spawn entity
        protocol.registerClientbound(ClientboundPackets1_15.ADD_GLOBAL_ENTITY, ClientboundPackets1_16.ADD_ENTITY, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            byte type = wrapper.read(Types.BYTE);
            if (type != 1) {
                // Cancel if not lightning/invalid id
                wrapper.cancel();
                return;
            }

            wrapper.user().getEntityTracker(Protocol1_15_2To1_16.class).addEntity(entityId, EntityTypes1_16.LIGHTNING_BOLT);

            wrapper.write(Types.UUID, UUID.randomUUID()); // uuid
            wrapper.write(Types.VAR_INT, EntityTypes1_16.LIGHTNING_BOLT.getId()); // entity type

            wrapper.passthrough(Types.DOUBLE); // x
            wrapper.passthrough(Types.DOUBLE); // y
            wrapper.passthrough(Types.DOUBLE); // z
            wrapper.write(Types.BYTE, (byte) 0); // yaw
            wrapper.write(Types.BYTE, (byte) 0); // pitch
            wrapper.write(Types.INT, 0); // data
            wrapper.write(Types.SHORT, (short) 0); // velocity
            wrapper.write(Types.SHORT, (short) 0); // velocity
            wrapper.write(Types.SHORT, (short) 0); // velocity
        });

        metadataRewriter.registerTrackerWithData(ClientboundPackets1_15.ADD_ENTITY, EntityTypes1_16.FALLING_BLOCK);
        metadataRewriter.registerTracker(ClientboundPackets1_15.ADD_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_15.ADD_PLAYER, EntityTypes1_16.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_15.SET_ENTITY_DATA, Types1_14.METADATA_LIST, Types1_16.METADATA_LIST);
        metadataRewriter.registerRemoveEntities(ClientboundPackets1_15.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_15.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(DIMENSION_HANDLER);
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Gamemode
                handler(wrapper -> {
                    wrapper.write(Types.BYTE, (byte) -1); // Previous gamemode, set to none

                    String levelType = wrapper.read(Types.STRING);
                    wrapper.write(Types.BOOLEAN, false); // debug
                    wrapper.write(Types.BOOLEAN, levelType.equals("flat"));
                    wrapper.write(Types.BOOLEAN, true); // keep all playerdata
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.UNSIGNED_BYTE); //  Gamemode
                handler(wrapper -> {
                    wrapper.write(Types.BYTE, (byte) -1); // Previous gamemode, set to none
                    wrapper.write(Types.STRING_ARRAY, Arrays.copyOf(WORLD_NAMES, WORLD_NAMES.length)); // World list - only used for command completion
                    wrapper.write(Types.NAMED_COMPOUND_TAG, DIMENSIONS_TAG.copy()); // Dimension registry
                });
                handler(DIMENSION_HANDLER); // Dimension
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Max players
                handler(wrapper -> {
                    wrapper.user().getEntityTracker(Protocol1_15_2To1_16.class).addEntity(wrapper.get(Types.INT, 0), EntityTypes1_16.PLAYER);

                    final String type = wrapper.read(Types.STRING);// level type
                    wrapper.passthrough(Types.VAR_INT); // View distance
                    wrapper.passthrough(Types.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Types.BOOLEAN); // Show death screen

                    wrapper.write(Types.BOOLEAN, false); // Debug
                    wrapper.write(Types.BOOLEAN, type.equals("flat"));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.UPDATE_ATTRIBUTES, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int size = wrapper.passthrough(Types.INT);
            int actualSize = size;
            for (int i = 0; i < size; i++) {
                // Attributes have been renamed and are now namespaced identifiers
                String key = wrapper.read(Types.STRING);
                String attributeIdentifier = AttributeMappings.attributeIdentifierMappings().get(key);
                if (attributeIdentifier == null) {
                    attributeIdentifier = Key.namespaced(key);
                    if (!Key.isValid(attributeIdentifier)) {
                        if (!Via.getConfig().isSuppressConversionWarnings()) {
                            Via.getPlatform().getLogger().warning("Invalid attribute: " + key);
                        }
                        actualSize--;
                        wrapper.read(Types.DOUBLE);
                        int modifierSize = wrapper.read(Types.VAR_INT);
                        for (int j = 0; j < modifierSize; j++) {
                            wrapper.read(Types.UUID);
                            wrapper.read(Types.DOUBLE);
                            wrapper.read(Types.BYTE);
                        }
                        continue;
                    }
                }

                wrapper.write(Types.STRING, attributeIdentifier);

                wrapper.passthrough(Types.DOUBLE);
                int modifierSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Types.UUID);
                    wrapper.passthrough(Types.DOUBLE);
                    wrapper.passthrough(Types.BYTE);
                }
            }
            if (size != actualSize) {
                wrapper.set(Types.INT, 0, actualSize);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_16.SWING, wrapper -> {
            InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
            // Don't send an arm swing if the player has an inventory opened.
            if (inventoryTracker.isInventoryOpen()) {
                wrapper.cancel();
            }
        });
    }
}
