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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.google.common.collect.Maps;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityPacketRewriter1_19 extends EntityRewriter<ClientboundPackets1_18, Protocol1_18_2To1_19> {

    public EntityPacketRewriter1_19(final Protocol1_18_2To1_19 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTracker(ClientboundPackets1_18.ADD_PLAYER, EntityTypes1_19.PLAYER);
        registerSetEntityData(ClientboundPackets1_18.SET_ENTITY_DATA, Types1_18.ENTITY_DATA_LIST, Types1_19.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_18.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_18.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.UUID); // Entity UUID
                map(Types.VAR_INT); // Entity type
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.BYTE); // Pitch
                map(Types.BYTE); // Yaw
                handler(wrapper -> {
                    final byte yaw = wrapper.get(Types.BYTE, 1);
                    wrapper.write(Types.BYTE, yaw); // Head yaw
                });
                map(Types.INT, Types.VAR_INT); // Data
                handler(trackerHandler());
                handler(wrapper -> {
                    final int entityId = wrapper.get(Types.VAR_INT, 0);
                    final EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == EntityTypes1_19.FALLING_BLOCK) {
                        wrapper.set(Types.VAR_INT, 2, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.VAR_INT, 2)));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.ADD_PAINTING, ClientboundPackets1_19.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.UUID); // Entity UUID
                handler(wrapper -> {
                    wrapper.write(Types.VAR_INT, EntityTypes1_19.PAINTING.getId());

                    final int motive = wrapper.read(Types.VAR_INT);
                    final BlockPosition blockPosition = wrapper.read(Types.BLOCK_POSITION1_14);
                    final byte direction = wrapper.read(Types.BYTE);
                    wrapper.write(Types.DOUBLE, blockPosition.x() + 0.5d);
                    wrapper.write(Types.DOUBLE, blockPosition.y() + 0.5d);
                    wrapper.write(Types.DOUBLE, blockPosition.z() + 0.5d);
                    wrapper.write(Types.BYTE, (byte) 0); // Pitch
                    wrapper.write(Types.BYTE, (byte) 0); // Yaw
                    wrapper.write(Types.BYTE, (byte) 0); // Head yaw
                    wrapper.write(Types.VAR_INT, to3dId(direction)); // Data
                    wrapper.write(Types.SHORT, (short) 0); // Velocity x
                    wrapper.write(Types.SHORT, (short) 0); // Velocity y
                    wrapper.write(Types.SHORT, (short) 0); // Velocity z

                    wrapper.send(Protocol1_18_2To1_19.class);
                    wrapper.cancel();

                    // Send motive in entity data
                    final PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_19.SET_ENTITY_DATA);
                    entityDataPacket.write(Types.VAR_INT, wrapper.get(Types.VAR_INT, 0)); // Entity id
                    final List<EntityData> entityData = new ArrayList<>();
                    entityData.add(new EntityData(8, Types1_19.ENTITY_DATA_TYPES.paintingVariantType, protocol.getMappingData().getPaintingMappings().getNewIdOrDefault(motive, 0)));
                    entityDataPacket.write(Types1_19.ENTITY_DATA_LIST, entityData);
                    entityDataPacket.send(Protocol1_18_2To1_19.class);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.ADD_MOB, ClientboundPackets1_19.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity ID
                map(Types.UUID); // Entity UUID
                map(Types.VAR_INT); // Entity Type
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                handler(wrapper -> {
                    // Change order
                    final byte yaw = wrapper.read(Types.BYTE);
                    final byte pitch = wrapper.read(Types.BYTE);
                    wrapper.write(Types.BYTE, pitch);
                    wrapper.write(Types.BYTE, yaw);
                });
                map(Types.BYTE); // Head yaw
                create(Types.VAR_INT, 0); // Data
                map(Types.SHORT); // Velocity x
                map(Types.SHORT); // Velocity y
                map(Types.SHORT); // Velocity z
                handler(trackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.UPDATE_MOB_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.VAR_INT); // Effect id
                map(Types.BYTE); // Amplifier
                map(Types.VAR_INT); // Duration
                map(Types.BYTE); // Flags
                create(Types.OPTIONAL_NAMED_COMPOUND_TAG, null); // No factor data
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Registry
                handler(wrapper -> {
                    final CompoundTag tag = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);

                    // Add necessary chat types
                    tag.put("minecraft:chat_type", protocol.getMappingData().chatRegistry());

                    // Cache a whole lot of data
                    final ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(tag, "dimension_type");
                    final Map<String, DimensionData> dimensionDataMap = new HashMap<>(dimensions.size());
                    final Map<CompoundTag, String> dimensionsMap = new HashMap<>(dimensions.size());
                    for (final CompoundTag dimension : dimensions) {
                        final NumberTag idTag = dimension.getNumberTag("id");
                        final CompoundTag element = dimension.getCompoundTag("element");
                        final String name = dimension.getStringTag("name").getValue();
                        addMonsterSpawnData(element);
                        dimensionDataMap.put(Key.stripMinecraftNamespace(name), new DimensionDataImpl(idTag.asInt(), element));
                        dimensionsMap.put(element.copy(), name);
                    }
                    tracker(wrapper.user()).setDimensions(dimensionDataMap);

                    final DimensionRegistryStorage registryStorage = wrapper.user().get(DimensionRegistryStorage.class);
                    registryStorage.setDimensions(dimensionsMap);
                    writeDimensionKey(wrapper, registryStorage);
                });
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.VAR_INT); // Max players
                map(Types.VAR_INT); // Chunk radius
                map(Types.VAR_INT); // Simulation distance
                map(Types.BOOLEAN); // Reduced debug info
                map(Types.BOOLEAN); // Show death screen
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                create(Types.OPTIONAL_GLOBAL_POSITION, null); // Last death location
                handler(playerTrackerHandler());
                handler(worldDataTrackerHandlerByKey());
                handler(biomeSizeTracker());
                handler(wrapper -> {
                    // Disable the chat preview
                    final PacketWrapper displayPreviewPacket = wrapper.create(ClientboundPackets1_19.SET_DISPLAY_CHAT_PREVIEW);
                    displayPreviewPacket.write(Types.BOOLEAN, false);
                    displayPreviewPacket.scheduleSend(Protocol1_18_2To1_19.class);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_18.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> writeDimensionKey(wrapper, wrapper.user().get(DimensionRegistryStorage.class)));
                map(Types.STRING); // World
                map(Types.LONG); // Seed
                map(Types.UNSIGNED_BYTE); // Gamemode
                map(Types.BYTE); // Previous gamemode
                map(Types.BOOLEAN); // Debug
                map(Types.BOOLEAN); // Flat
                map(Types.BOOLEAN); // Keep player attributes
                create(Types.OPTIONAL_GLOBAL_POSITION, null); // Last death location
                handler(worldDataTrackerHandlerByKey());
            }
        });
    }

    private void writeDimensionKey(final PacketWrapper wrapper, final DimensionRegistryStorage registryStorage) {
        // Find dimension key by data
        final CompoundTag currentDimension = wrapper.read(Types.NAMED_COMPOUND_TAG);
        addMonsterSpawnData(currentDimension);
        String dimensionKey = registryStorage.dimensionKey(currentDimension);
        if (dimensionKey == null) {
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                protocol.getLogger().warning("The server tried to send dimension data from a dimension the client wasn't told about on join. " +
                    "Plugins and mods have to make sure they are not creating new dimension types while players are online, and proxies need to make sure they don't scramble dimension data." +
                    " Received dimension: " + currentDimension + ". Known dimensions: " + registryStorage.dimensions());
            }

            // Try to find the most similar dimension
            dimensionKey = registryStorage.dimensions().entrySet().stream()
                .map(it -> new Pair<>(it, Maps.difference(currentDimension.getValue(), it.getKey().getValue()).entriesInCommon()))
                .filter(it -> it.value().containsKey("min_y") && it.value().containsKey("height"))
                .max(Comparator.comparingInt(it -> it.value().size()))
                .orElseThrow(() -> new IllegalArgumentException("Dimension not found in registry data from join packet: " + currentDimension))
                .key().getValue();
        }

        wrapper.write(Types.STRING, dimensionKey);
    }

    private int to3dId(final int id) {
        return switch (id) {
            case -1 -> 1; // Up/down -> Up
            case 2 -> 2; // North
            case 0 -> 3; // South
            case 1 -> 4; // West
            case 3 -> 5; // East
            default -> throw new IllegalArgumentException("Unknown 2d id: " + id);
        };
    }

    private void addMonsterSpawnData(final CompoundTag dimension) {
        // The actual values here don't matter
        dimension.put("monster_spawn_block_light_limit", new IntTag(0));
        dimension.put("monster_spawn_light_level", new IntTag(11));
    }

    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_19.ENTITY_DATA_TYPES::byId);
        filter().dataType(Types1_19.ENTITY_DATA_TYPES.particleType).handler((event, data) -> {
            final Particle particle = (Particle) data.getValue();
            final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
            if (particle.id() == particleMappings.id("vibration")) {
                // Remove the position
                particle.getArguments().remove(0);

                final String resourceLocation = Key.stripMinecraftNamespace(particle.<String>getArgument(0).getValue());
                if (resourceLocation.equals("entity")) {
                    // Add Y offset
                    particle.getArguments().add(2, new Particle.ParticleData<>(Types.FLOAT, 0F));
                }
            }

            protocol.getParticleRewriter().rewriteParticle(event.user(), particle);
        });

        registerEntityDataTypeHandler(Types1_19.ENTITY_DATA_TYPES.itemType, Types1_19.ENTITY_DATA_TYPES.optionalBlockStateType, null);
        registerBlockStateHandler(EntityTypes1_19.ABSTRACT_MINECART, 11);

        filter().type(EntityTypes1_19.CAT).index(19).mapDataType(typeId -> Types1_19.ENTITY_DATA_TYPES.catVariantType);
    }

    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }

    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_19.getTypeFromId(type);
    }
}
