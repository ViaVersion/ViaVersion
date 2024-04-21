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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityPackets extends EntityRewriter<ClientboundPackets1_18, Protocol1_19To1_18_2> {

    public EntityPackets(final Protocol1_19To1_18_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerTracker(ClientboundPackets1_18.SPAWN_PLAYER, EntityTypes1_19.PLAYER);
        registerMetadataRewriter(ClientboundPackets1_18.ENTITY_METADATA, Types1_18.METADATA_LIST, Types1_19.METADATA_LIST);
        registerRemoveEntities(ClientboundPackets1_18.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
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
                    if (entityType == EntityTypes1_19.FALLING_BLOCK) {
                        wrapper.set(Type.VAR_INT, 2, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.VAR_INT, 2)));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_PAINTING, ClientboundPackets1_19.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.UUID); // Entity UUID
                handler(wrapper -> {
                    wrapper.write(Type.VAR_INT, EntityTypes1_19.PAINTING.getId());

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
                    metadata.add(new Metadata(8, Types1_19.META_TYPES.paintingVariantType, protocol.getMappingData().getPaintingMappings().getNewIdOrDefault(motive, 0)));
                    metaPacket.write(Types1_19.METADATA_LIST, metadata);
                    metaPacket.send(Protocol1_19To1_18_2.class);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.SPAWN_MOB, ClientboundPackets1_19.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity ID
                map(Type.UUID); // Entity UUID
                map(Type.VAR_INT); // Entity Type
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                handler(wrapper -> {
                    // Change order
                    final byte yaw = wrapper.read(Type.BYTE);
                    final byte pitch = wrapper.read(Type.BYTE);
                    wrapper.write(Type.BYTE, pitch);
                    wrapper.write(Type.BYTE, yaw);
                });
                map(Type.BYTE); // Head yaw
                create(Type.VAR_INT, 0); // Data
                map(Type.SHORT); // Velocity x
                map(Type.SHORT); // Velocity y
                map(Type.SHORT); // Velocity z
                handler(trackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.ENTITY_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.VAR_INT); // Effect id
                map(Type.BYTE); // Amplifier
                map(Type.VAR_INT); // Duration
                map(Type.BYTE); // Flags
                create(Type.OPTIONAL_NAMED_COMPOUND_TAG, null); // No factor data
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NAMED_COMPOUND_TAG); // Registry
                handler(wrapper -> {
                    final CompoundTag tag = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);

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
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.VAR_INT); // Max players
                map(Type.VAR_INT); // Chunk radius
                map(Type.VAR_INT); // Simulation distance
                map(Type.BOOLEAN); // Reduced debug info
                map(Type.BOOLEAN); // Show death screen
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                create(Type.OPTIONAL_GLOBAL_POSITION, null); // Last death location
                handler(playerTrackerHandler());
                handler(worldDataTrackerHandlerByKey());
                handler(biomeSizeTracker());
                handler(wrapper -> {
                    // Disable the chat preview
                    final PacketWrapper displayPreviewPacket = wrapper.create(ClientboundPackets1_19.SET_DISPLAY_CHAT_PREVIEW);
                    displayPreviewPacket.write(Type.BOOLEAN, false);
                    displayPreviewPacket.scheduleSend(Protocol1_19To1_18_2.class);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_18.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> writeDimensionKey(wrapper, wrapper.user().get(DimensionRegistryStorage.class)));
                map(Type.STRING); // World
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous gamemode
                map(Type.BOOLEAN); // Debug
                map(Type.BOOLEAN); // Flat
                map(Type.BOOLEAN); // Keep player data
                create(Type.OPTIONAL_GLOBAL_POSITION, null); // Last death location
                handler(worldDataTrackerHandlerByKey());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_18.PLAYER_INFO, wrapper -> {
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
                        wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
                    }

                    wrapper.passthrough(Type.VAR_INT); // Gamemode
                    wrapper.passthrough(Type.VAR_INT); // Ping
                    final JsonElement displayName = wrapper.read(Type.OPTIONAL_COMPONENT); // Display name
                    if (!Protocol1_19To1_18_2.isTextComponentNull(displayName)) {
                        wrapper.write(Type.OPTIONAL_COMPONENT, displayName);
                    } else {
                        wrapper.write(Type.OPTIONAL_COMPONENT, null);
                    }

                    // No public profile signature
                    wrapper.write(Type.OPTIONAL_PROFILE_KEY, null);
                } else if (action == 1 || action == 2) { // Update gamemode/update latency
                    wrapper.passthrough(Type.VAR_INT);
                } else if (action == 3) { // Update display name
                    final JsonElement displayName = wrapper.read(Type.OPTIONAL_COMPONENT); // Display name
                    if (!Protocol1_19To1_18_2.isTextComponentNull(displayName)) {
                        wrapper.write(Type.OPTIONAL_COMPONENT, displayName);
                    } else {
                        wrapper.write(Type.OPTIONAL_COMPONENT, null);
                    }
                }
            }
        });
    }

    private static void writeDimensionKey(final PacketWrapper wrapper, final DimensionRegistryStorage registryStorage) throws Exception {
        // Find dimension key by data
        final CompoundTag currentDimension = wrapper.read(Type.NAMED_COMPOUND_TAG);
        addMonsterSpawnData(currentDimension);
        String dimensionKey = registryStorage.dimensionKey(currentDimension);
        if (dimensionKey == null) {
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                Via.getPlatform().getLogger().warning("The server tried to send dimension data from a dimension the client wasn't told about on join. " +
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
        filter().mapMetaType(Types1_19.META_TYPES::byId);
        filter().metaType(Types1_19.META_TYPES.particleType).handler((event, meta) -> {
            final Particle particle = (Particle) meta.getValue();
            final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
            if (particle.getId() == particleMappings.id("vibration")) {
                // Remove the position
                particle.getArguments().remove(0);

                final String resourceLocation = Key.stripMinecraftNamespace(particle.<String>getArgument(0).getValue());
                if (resourceLocation.equals("entity")) {
                    // Add Y offset
                    particle.getArguments().add(2, new Particle.ParticleData<>(Type.FLOAT, 0F));
                }
            }

            rewriteParticle(event.user(), particle);
        });

        registerMetaTypeHandler(Types1_19.META_TYPES.itemType, Types1_19.META_TYPES.blockStateType, null);

        filter().type(EntityTypes1_19.MINECART_ABSTRACT).index(11).handler((event, meta) -> {
            // Convert to new block id
            final int data = (int) meta.getValue();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        filter().type(EntityTypes1_19.CAT).index(19).mapMetaType(typeId -> Types1_19.META_TYPES.catVariantType);
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
