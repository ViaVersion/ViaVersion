/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityPackets extends EntityRewriter<Protocol1_19To1_18_2> {

    //TODO move to compressed nbt file
    private static final String CHAT_REGISTRY_SNBT = "{\n" +
            "   \"minecraft:chat_type\":{\n" +
            "      \"type\":\"minecraft:chat_type\",\n" +
            "      \"value\":[\n" +
            "         {\n" +
            "            \"name\":\"minecraft:chat\",\n" +
            "            \"id\":0,\n" +
            "            \"element\":{\n" +
            "               \"chat\":{\n" +
            "                  \"decoration\":{\n" +
            "                     \"translation_key\":\"chat.type.text\",\n" +
            "                     \"style\":{\n" +
            "                        \n" +
            "                     },\n" +
            "                     \"parameters\":[\n" +
            "                        \"sender\",\n" +
            "                        \"content\"\n" +
            "                     ]\n" +
            "                  }\n" +
            "               },\n" +
            "               \"narration\":{\n" +
            "                  \"priority\":\"chat\",\n" +
            "                  \"decoration\":{\n" +
            "                     \"translation_key\":\"chat.type.text.narrate\",\n" +
            "                     \"style\":{\n" +
            "                        \n" +
            "                     },\n" +
            "                     \"parameters\":[\n" +
            "                        \"sender\",\n" +
            "                        \"content\"\n" +
            "                     ]\n" +
            "                  }\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         {\n" +
            "            \"name\":\"minecraft:system\",\n" +
            "            \"id\":1,\n" +
            "            \"element\":{\n" +
            "               \"chat\":{\n" +
            "                  \n" +
            "               },\n" +
            "               \"narration\":{\n" +
            "                  \"priority\":\"system\"\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         {\n" +
            "            \"name\":\"minecraft:game_info\",\n" +
            "            \"id\":2,\n" +
            "            \"element\":{\n" +
            "               \"overlay\":{\n" +
            "                  \n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "      ]\n" +
            "   },\n" +
            "}";
    private static final CompoundTag CHAT_REGISTRY;

    static {
        try {
            CHAT_REGISTRY = BinaryTagIO.readString(CHAT_REGISTRY_SNBT).get("minecraft:chat_type");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityPackets(final Protocol1_19To1_18_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTracker(ClientboundPackets1_18.SPAWN_PLAYER, Entity1_19Types.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_18.ENTITY_METADATA, Types1_18.METADATA_LIST, Types1_19.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_18.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                map(Type.UUID); // Entity UUID
                map(Type.VAR_INT); // Entity type
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.BYTE); // Pitch
                map(Type.BYTE); // Yaw
                handler(wrapper -> {
                    final byte yaw = wrapper.get(Type.BYTE, 1);
                    wrapper.write(Type.BYTE, yaw); // Head yaw
                });
                map(Type.INT, Type.VAR_INT); // Data
                handler(trackerHandler());
                handler(wrapper -> {
                    final int entityId = wrapper.get(Type.VAR_INT, 0);
                    final EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == Entity1_19Types.FALLING_BLOCK) {
                        wrapper.set(Type.VAR_INT, 2, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.VAR_INT, 2)));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_PAINTING, ClientboundPackets1_19.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                map(Type.UUID); // Entity UUID
                handler(wrapper -> {
                    wrapper.write(Type.VAR_INT, Entity1_19Types.PAINTING.getId());

                    final int motive = wrapper.read(Type.VAR_INT);
                    final Position blockPosition = wrapper.read(Type.POSITION1_14);
                    final byte direction = wrapper.read(Type.BYTE);
                    wrapper.write(Type.DOUBLE, blockPosition.x() + 0.5d);
                    wrapper.write(Type.DOUBLE, blockPosition.y() + 0.5d);
                    wrapper.write(Type.DOUBLE, blockPosition.z() + 0.5d);
                    wrapper.write(Type.BYTE, (byte) 0); // Pitch
                    wrapper.write(Type.BYTE, (byte) 0); // Yaw
                    wrapper.write(Type.BYTE, (byte) 0); // Head yaw
                    wrapper.write(Type.VAR_INT, to3dId(direction)); // Data
                    wrapper.write(Type.SHORT, (short) 0); // Velocity x
                    wrapper.write(Type.SHORT, (short) 0); // Velocity y
                    wrapper.write(Type.SHORT, (short) 0); // Velocity z

                    wrapper.send(Protocol1_19To1_18_2.class);
                    wrapper.cancel();

                    // Send motive in metadata
                    final PacketWrapper metaPacket = wrapper.create(ClientboundPackets1_19.ENTITY_METADATA);
                    metaPacket.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0)); // Entity id
                    final List<Metadata> metadata = new ArrayList<>();
                    metadata.add(new Metadata(8, Types1_19.META_TYPES.paintingVariantType, protocol.getMappingData().getPaintingMappings().getNewId(motive)));
                    metaPacket.write(Types1_19.METADATA_LIST, metadata);
                    metaPacket.send(Protocol1_19To1_18_2.class);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_MOB, ClientboundPackets1_19.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity ID
                map(Type.UUID); // Entity UUID
                map(Type.VAR_INT); // Entity Type
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.BYTE); // Yaw
                map(Type.BYTE); // Pitch
                map(Type.BYTE); // Head yaw
                create(Type.VAR_INT, 0); // Data
                map(Type.SHORT); // Velocity x
                map(Type.SHORT); // Velocity y
                map(Type.SHORT); // Velocity z
                handler(trackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.ENTITY_EFFECT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                map(Type.VAR_INT); // Effect id
                map(Type.BYTE); // Amplifier
                map(Type.VAR_INT); // Duration
                map(Type.BYTE); // Flags
                create(Type.BOOLEAN, false); // No factor data
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Registry
                handler(wrapper -> {
                    final CompoundTag tag = wrapper.get(Type.NBT, 0);

                    // Add necessary chat types
                    tag.put("minecraft:chat_type", CHAT_REGISTRY.clone());

                    // Cache a whole lot of data
                    final ListTag dimensions = ((CompoundTag) tag.get("minecraft:dimension_type")).get("value");
                    final Map<String, DimensionData> dimensionDataMap = new HashMap<>(dimensions.size());
                    final Map<CompoundTag, String> dimensionsMap = new HashMap<>(dimensions.size());
                    for (final Tag dimension : dimensions) {
                        final CompoundTag dimensionCompound = (CompoundTag) dimension;
                        final CompoundTag element = dimensionCompound.get("element");
                        addMonsterSpawnData(element);

                        final String name = (String) dimensionCompound.get("name").getValue();
                        dimensionDataMap.put(name, new DimensionDataImpl(element));
                        dimensionsMap.put(element, name);
                    }
                    tracker(wrapper.user()).setDimensions(dimensionDataMap);

                    final DimensionRegistryStorage registryStorage = wrapper.user().get(DimensionRegistryStorage.class);
                    registryStorage.setDimensions(dimensionsMap);
                    writeDimensionKey(wrapper, registryStorage);
                });
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // Chunk radius
                map(Type.VAR_INT); // Simulation distance
                handler(playerTrackerHandler());
                handler(worldDataTrackerHandlerByKey());
                handler(biomeSizeTracker());
                handler(wrapper -> {
                    // Disable the chat preview
                    final PacketWrapper displayPreviewPacket = wrapper.create(ClientboundPackets1_19.SET_DISPLAY_CHAT_PREVIEW);
                    displayPreviewPacket.write(Type.BOOLEAN, false);
                    displayPreviewPacket.send(Protocol1_19To1_18_2.class);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_18.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> writeDimensionKey(wrapper, wrapper.user().get(DimensionRegistryStorage.class)));
                map(Type.STRING); // World
                handler(worldDataTrackerHandlerByKey());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.PLAYER_INFO, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int action = wrapper.passthrough(Type.VAR_INT);
                    final int entries = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < entries; i++) {
                        wrapper.passthrough(Type.UUID); // UUID
                        if (action == 0) { // Add player
                            wrapper.passthrough(Type.STRING); // Player Name

                            final int properties = wrapper.passthrough(Type.VAR_INT);
                            for (int j = 0; j < properties; j++) {
                                wrapper.passthrough(Type.STRING); // Name
                                wrapper.passthrough(Type.STRING); // Value
                                if (wrapper.passthrough(Type.BOOLEAN)) {
                                    wrapper.passthrough(Type.STRING); // Signature
                                }
                            }

                            wrapper.passthrough(Type.VAR_INT); // Gamemode
                            wrapper.passthrough(Type.VAR_INT); // Ping
                            if (wrapper.passthrough(Type.BOOLEAN)) {
                                wrapper.passthrough(Type.COMPONENT); // Display name
                            }

                            // No public profile signature
                            wrapper.write(Type.BOOLEAN, false);
                        } else if (action == 1 || action == 2) { // Update gamemode/update latency
                            wrapper.passthrough(Type.VAR_INT);
                        } else if (action == 3) { // Update display name
                            if (wrapper.passthrough(Type.BOOLEAN)) {
                                wrapper.passthrough(Type.COMPONENT);
                            }
                        }
                    }
                });
            }
        });
    }

    private static void writeDimensionKey(PacketWrapper wrapper, DimensionRegistryStorage registryStorage) throws Exception {
        // Find dimension key by data
        final CompoundTag currentDimension = wrapper.read(Type.NBT);
        addMonsterSpawnData(currentDimension);

        final String dimensionKey = registryStorage.dimensionKey(currentDimension);
        if (dimensionKey == null) {
            throw new IllegalArgumentException("Unknown dimension sent on join: " + currentDimension);
        }

        wrapper.write(Type.STRING, dimensionKey);
    }

    private static int to3dId(final int id) {
        switch (id) {
            case -1: // Both up and down
                return 1; // Up
            case 2: // North
                return 2;
            case 0: // South
                return 3;
            case 1: // West
                return 4;
            case 3: // East
                return 5;
        }
        throw new IllegalArgumentException("Unknown 2d id: " + id);
    }

    private static void addMonsterSpawnData(final CompoundTag dimension) {
        // The actual values here don't matter
        dimension.put("monster_spawn_block_light_limit", new IntTag(0));
        dimension.put("monster_spawn_light_level", new IntTag(11));
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> meta.setMetaType(Types1_19.META_TYPES.byId(meta.metaType().typeId())));

        registerMetaTypeHandler(Types1_19.META_TYPES.itemType, Types1_19.META_TYPES.blockStateType, Types1_19.META_TYPES.particleType);

        filter().filterFamily(Entity1_19Types.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            // Convert to new block id
            final int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        filter().type(Entity1_19Types.PLAYER).addIndex(19); // Last death location
        filter().type(Entity1_19Types.CAT).index(19).handler((event, meta) -> meta.setMetaType(Types1_19.META_TYPES.catVariantType));
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return Entity1_19Types.getTypeFromId(type);
    }
}
