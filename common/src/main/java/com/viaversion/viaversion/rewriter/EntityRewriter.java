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
package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.Int2IntMapMappings;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataFilter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEventImpl;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class EntityRewriter<C extends ClientboundPacketType, T extends Protocol<C, ?, ?, ?>>
    extends RewriterBase<T> implements com.viaversion.viaversion.api.rewriter.EntityRewriter<T> {
    protected final List<EntityDataFilter> entityDataFilters = new ArrayList<>();
    protected final boolean trackMappedType;
    protected Mappings typeMappings;

    protected EntityRewriter(T protocol) {
        this(protocol, true);
    }

    /**
     * Creates a new entity rewriter instance.
     *
     * @param protocol        protocol
     * @param trackMappedType whether the mapped or unmapped entity type should be tracked
     */
    protected EntityRewriter(T protocol, boolean trackMappedType) {
        super(protocol);
        this.trackMappedType = trackMappedType;
        protocol.put(this);
    }

    /**
     * Returns an entity data filter builder.
     * <p>
     * Calling {@link EntityDataFilter.Builder#register()} will automatically register the filter on this rewriter.
     *
     * @return entity data filter builder
     */
    public EntityDataFilter.Builder filter() {
        return new EntityDataFilter.Builder(this);
    }

    /**
     * Registers an entity data filter.
     * Note that {@link EntityDataFilter.Builder#register()} already calls this method.
     *
     * @param filter filter to register
     * @throws IllegalArgumentException if the filter is already registered
     */
    public void registerFilter(EntityDataFilter filter) {
        Preconditions.checkArgument(!entityDataFilters.contains(filter));
        entityDataFilters.add(filter);
    }

    @Override
    public void handleEntityData(final int entityId, final List<EntityData> dataList, final UserConnection connection) {
        final TrackedEntity entity = tracker(connection).entity(entityId);
        final EntityType type = entity != null ? entity.entityType() : null;

        // Iterate over indexed list to allow for removal and addition of elements, decrease current index and size if an element is removed
        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            final EntityData entityData = dataList.get(i);
            EntityDataHandlerEvent event = null;
            for (final EntityDataFilter filter : entityDataFilters) {
                if (!filter.isFiltered(type, entityData)) {
                    continue;
                }
                if (event == null) {
                    // Instantiate lazily and share event instance
                    event = new EntityDataHandlerEventImpl(connection, entity, entityId, entityData, dataList);
                }

                try {
                    filter.handler().handle(event, entityData);
                } catch (final Exception e) {
                    logException(e, type, dataList, entityData);
                    dataList.remove(i--);
                    size--;
                    break;
                }

                if (event.cancelled()) {
                    // Remove entity data, and break current filter loop
                    dataList.remove(i--);
                    size--;
                    break;
                }
            }

            if (event != null && event.hasExtraData()) {
                // Finally, add newly created entity data
                dataList.addAll(event.extraData());
            }
        }

        if (entity != null) {
            entity.sentEntityData(true);
        }
    }

    @Override
    public int newEntityId(int id) {
        return typeMappings != null ? typeMappings.getNewIdOrDefault(id, id) : id;
    }

    @Override
    public String mappedEntityIdentifier(final String identifier) {
        if (typeMappings instanceof final FullMappings fullMappings) {
            final String mappedIdentifier = fullMappings.mappedIdentifier(identifier);
            if (mappedIdentifier != null) {
                return mappedIdentifier;
            }
        }
        return identifier;
    }

    /**
     * Maps an entity type.
     *
     * @param type       entity type
     * @param mappedType mapped entity type
     * @throws IllegalArgumentException if the types share the same implementing class
     */
    public void mapEntityType(EntityType type, EntityType mappedType) {
        Preconditions.checkArgument(type.getClass() != mappedType.getClass(), "EntityTypes should not be of the same class/enum");
        mapEntityType(type.getId(), mappedType.getId());
    }

    protected void mapEntityType(int id, int mappedId) {
        if (typeMappings == null) {
            typeMappings = Int2IntMapMappings.of();
        }
        typeMappings.setNewId(id, mappedId);
    }

    /**
     * Maps entity ids based on the protocol's mapping data.
     */
    public void mapTypes() {
        Preconditions.checkArgument(typeMappings == null, "Type mappings have already been set - manual type mappings should be set *after* this");
        Preconditions.checkNotNull(protocol.getMappingData().getEntityMappings(), "Protocol does not have entity mappings");
        typeMappings = protocol.getMappingData().getEntityMappings();
    }

    /**
     * Registers an entity data handler to rewrite, item, block, and particle ids stored in entity data.
     *
     * @param itemType       item data type if needed
     * @param blockStateType block state data type if needed
     * @param particleType   particle data type if needed
     */
    public void registerEntityDataTypeHandler(@Nullable EntityDataType itemType, @Nullable EntityDataType blockStateType, @Nullable EntityDataType particleType) {
        registerEntityDataTypeHandler(itemType, null, blockStateType, particleType, null);
    }

    public void registerEntityDataTypeHandler(@Nullable EntityDataType itemType, @Nullable EntityDataType blockStateType, @Nullable EntityDataType optionalBlockStateType, @Nullable EntityDataType particleType, @Nullable EntityDataType particlesType) {
        registerEntityDataTypeHandler(itemType, blockStateType, optionalBlockStateType, particleType, particlesType, null, null);
    }

    /**
     * Registers an entity data handler to rewrite, item, block, and particle ids stored in entity data.
     *
     * @param itemType               item data type if needed
     * @param blockStateType         block state data type if needed
     * @param optionalBlockStateType optional block state data type if needed
     * @param particleType           particle data type if needed
     * @param particlesType          particles data type if needed
     * @param componentType          component data type if needed
     * @param optionalComponentType  optional component data type if needed
     */
    public void registerEntityDataTypeHandler(@Nullable EntityDataType itemType, @Nullable EntityDataType blockStateType, @Nullable EntityDataType optionalBlockStateType,
                                              @Nullable EntityDataType particleType, @Nullable EntityDataType particlesType,
                                              @Nullable EntityDataType componentType, @Nullable EntityDataType optionalComponentType) {
        filter().handler((event, data) -> {
            final EntityDataType type = data.dataType();
            if (type == itemType) {
                data.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), data.value()));
            } else if (type == blockStateType) {
                int value = data.value();
                data.setValue(protocol.getMappingData().getNewBlockStateId(value));
            } else if (type == optionalBlockStateType) {
                int value = data.value();
                if (value != 0) {
                    data.setValue(protocol.getMappingData().getNewBlockStateId(value));
                }
            } else if (type == particleType) {
                protocol.getParticleRewriter().rewriteParticle(event.user(), data.value());
            } else if (type == particlesType) {
                final Particle[] particles = data.value();
                for (final Particle particle : particles) {
                    protocol.getParticleRewriter().rewriteParticle(event.user(), particle);
                }
            } else if (type == componentType || type == optionalComponentType) {
                if (protocol.getComponentRewriter() != null) {
                    final Tag component = data.value();
                    protocol.getComponentRewriter().processTag(event.user(), component);
                }
            }
        });
    }

    public void registerBlockStateHandler(final EntityType entityType, final int index) {
        filter().type(entityType).index(index).handler((event, data) -> {
            final int state = (int) data.getValue();
            data.setValue(protocol.getMappingData().getNewBlockStateId(state));
        });
    }

    public void registerTracker(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity ID
            wrapper.passthrough(Types.UUID); // Entity UUID
            wrapper.passthrough(Types.VAR_INT); // Entity Type
            trackerHandler().handle(wrapper);
        });
    }

    public void registerTrackerWithData(C packetType, EntityType fallingBlockType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.UUID); // Entity UUID
                map(Types.VAR_INT); // Entity Type
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.BYTE); // Pitch
                map(Types.BYTE); // Yaw
                map(Types.INT); // Data
                handler(trackerHandler());
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == fallingBlockType) {
                        wrapper.set(Types.INT, 0, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.INT, 0)));
                    }
                });
            }
        });
    }

    public void registerTrackerWithData1_19(C packetType, EntityType fallingBlockType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.UUID); // Entity UUID
            final int entityTypeId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Head yaw
            final int data = wrapper.passthrough(Types.VAR_INT);

            final EntityType entityType = trackAndRewrite(wrapper, entityTypeId, entityId);
            if (protocol.getMappingData() != null && entityType == fallingBlockType) {
                final int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(data);
                wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
            }
        });
    }

    public void registerTrackerWithData1_21_9(C packetType, EntityType fallingBlockType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.UUID); // Entity UUID
            final int entityTypeId = wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.MOVEMENT_VECTOR); // Movement
            wrapper.passthrough(Types.BYTE); // Pitch
            wrapper.passthrough(Types.BYTE); // Yaw
            wrapper.passthrough(Types.BYTE); // Head yaw
            final int data = wrapper.passthrough(Types.VAR_INT);
            final EntityType entityType = trackAndRewrite(wrapper, entityTypeId, entityId);
            if (protocol.getMappingData() != null && entityType == fallingBlockType) {
                final int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(data);
                wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
            }
        });
    }

    /**
     * Registers an entity tracker for the extra spawn packets like player, painting, or xp orb spawns.
     *
     * @param packetType packet type
     * @param entityType entity type
     * @param intType    int type of the entity id
     */
    public void registerTracker(C packetType, EntityType entityType, Type<Integer> intType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int entityId = wrapper.passthrough(intType);
            tracker(wrapper.user()).addEntity(entityId, entityType);
        });
    }

    /**
     * Registers an entity tracker for the extra spawn packets.
     *
     * @param packetType packet type
     * @param entityType entity type
     */
    public void registerTracker(C packetType, EntityType entityType) {
        registerTracker(packetType, entityType, Types.VAR_INT);
    }

    public void registerRemoveEntities(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int[] entityIds = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            EntityTracker entityTracker = tracker(wrapper.user());
            for (int entity : entityIds) {
                entityTracker.removeEntity(entity);
            }
        });
    }

    public void registerSetEntityData(C packetType, @Nullable Type<List<EntityData>> dataType, Type<List<EntityData>> mappedDataType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            List<EntityData> entityData;
            if (dataType != null) {
                entityData = wrapper.read(dataType);
                wrapper.write(mappedDataType, entityData);
            } else {
                entityData = wrapper.passthrough(mappedDataType);
            }
            handleEntityData(entityId, entityData, wrapper.user());
        });
    }

    public void registerSetEntityData(C packetType, Type<List<EntityData>> dataType) {
        registerSetEntityData(packetType, null, dataType);
    }

    public void registerSetEntityData(C packetType) {
        registerSetEntityData(packetType, protocol.types().entityDataList(), protocol.mappedTypes().entityDataList());
    }

    public void registerLogin1_20_5(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int entityId = wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.BOOLEAN); // Hardcore
            wrapper.passthrough(Types.STRING_ARRAY); // World List
            wrapper.passthrough(Types.VAR_INT); // Max players
            wrapper.passthrough(Types.VAR_INT); // View distance
            wrapper.passthrough(Types.VAR_INT); // Simulation distance
            wrapper.passthrough(Types.BOOLEAN); // Reduced debug info
            wrapper.passthrough(Types.BOOLEAN); // Show death screen
            wrapper.passthrough(Types.BOOLEAN); // Limited crafting

            final int dimensionId = wrapper.passthrough(Types.VAR_INT);
            final String world = wrapper.passthrough(Types.STRING);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);

            wrapper.passthrough(Types.LONG); // Seed
            final byte gamemode = wrapper.passthrough(Types.BYTE);
            tracker(wrapper.user()).setInstaBuild(gamemode == GameMode.CREATIVE.id());

            trackPlayer(wrapper.user(), entityId);
        });
    }

    public void registerRespawn1_20_5(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int dimensionId = wrapper.passthrough(Types.VAR_INT);
            final String world = wrapper.passthrough(Types.STRING);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world); // Tracks world height and name for chunk data and entity (un)tracking

            wrapper.passthrough(Types.LONG); // Seed
            final byte gamemode = wrapper.passthrough(Types.BYTE);
            tracker(wrapper.user()).setInstaBuild(gamemode == GameMode.CREATIVE.id());
        });
    }

    // Track the insta payer build ability
    public void registerPlayerAbilities(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final byte flags = wrapper.passthrough(Types.BYTE);
            tracker(wrapper.user()).setInstaBuild((flags & 1 << 3) != 0);
        });
    }

    public void registerGameEvent(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            final short event = wrapper.passthrough(Types.UNSIGNED_BYTE);
            if (event == 3) {
                final int value = (int) Math.floor(wrapper.passthrough(Types.FLOAT) + 0.5F);
                tracker(wrapper.user()).setInstaBuild(value == GameMode.CREATIVE.id());
            }
        });
    }

    public PacketHandler trackerHandler() {
        return trackerAndRewriterHandler(null);
    }

    public PacketHandler playerTrackerHandler() {
        return wrapper -> trackPlayer(wrapper.user(), wrapper.get(Types.INT, 0));
    }

    public void trackPlayer(final UserConnection connection, final int entityId) {
        final EntityTracker tracker = tracker(connection);
        tracker.setClientEntityId(entityId);
        tracker.addEntity(entityId, tracker.playerType());
    }

    public void trackWorld(final UserConnection connection, final String world) {
        // Clear entities if the world changes
        final EntityTracker tracker = tracker(connection);
        if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
            tracker.clearEntities();
        }
        tracker.setCurrentWorld(world);
    }

    /**
     * Returns a packet handler storing height, min_y, and name of the current world.
     * If the client changes to a new world, the stored entity data will be cleared.
     *
     * @param nbtIndex index of the current world's nbt
     * @return packet handler
     */
    public PacketHandler worldDataTrackerHandler(int nbtIndex) {
        return wrapper -> {
            EntityTracker tracker = tracker(wrapper.user());

            CompoundTag registryData = wrapper.get(Types.NAMED_COMPOUND_TAG, nbtIndex);
            NumberTag height = registryData.getNumberTag("height");
            if (height != null) {
                int blockHeight = height.asInt();
                tracker.setCurrentWorldSectionHeight(blockHeight >> 4);
            } else {
                protocol.getLogger().warning("Height missing in dimension data: " + registryData);
            }

            NumberTag minY = registryData.getNumberTag("min_y");
            if (minY != null) {
                tracker.setCurrentMinY(minY.asInt());
            } else {
                protocol.getLogger().warning("Min Y missing in dimension data: " + registryData);
            }

            String world = wrapper.get(Types.STRING, 0);
            trackWorld(wrapper.user(), world);
        };
    }

    public PacketHandler worldDataTrackerHandlerByKey() {
        return wrapper -> {
            EntityTracker tracker = tracker(wrapper.user());
            String dimensionKey = wrapper.get(Types.STRING, 0);
            DimensionData dimensionData = tracker.dimensionData(dimensionKey);
            if (dimensionData == null) {
                protocol.getLogger().severe("Dimension data missing for dimension: " + dimensionKey + ", falling back to overworld");
                dimensionData = tracker.dimensionData("minecraft:overworld");
                Preconditions.checkNotNull(dimensionData, "Overworld data missing");
            }

            tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
            tracker.setCurrentMinY(dimensionData.minY());

            String world = wrapper.get(Types.STRING, 1);
            trackWorld(wrapper.user(), world);
        };
    }

    public PacketHandler worldDataTrackerHandlerByKey1_20_5(final int dimensionIdIndex) {
        return wrapper -> {
            int dimensionId = wrapper.get(Types.VAR_INT, dimensionIdIndex);
            String world = wrapper.get(Types.STRING, 0);
            trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
        };
    }

    public void trackWorldDataByKey1_20_5(final UserConnection connection, final int dimensionId, final String world) {
        // Track world height for use in chunk data
        EntityTracker tracker = tracker(connection);
        DimensionData dimensionData = tracker.dimensionData(dimensionId);
        if (dimensionData == null) {
            protocol.getLogger().severe("Dimension data missing for dimension: " + dimensionId + ", falling back to overworld");
            dimensionData = tracker.dimensionData("overworld");
            Preconditions.checkNotNull(dimensionData, "Overworld data missing");
        }
        tracker.setCurrentDimensionId(dimensionId);
        tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
        tracker.setCurrentMinY(dimensionData.minY());
        trackWorld(connection, world);
    }

    public PacketHandler biomeSizeTracker() {
        return wrapper -> trackBiomeSize(wrapper.user(), wrapper.get(Types.NAMED_COMPOUND_TAG, 0));
    }

    public PacketHandler configurationBiomeSizeTracker() {
        return wrapper -> trackBiomeSize(wrapper.user(), wrapper.get(Types.COMPOUND_TAG, 0));
    }

    public void trackBiomeSize(final UserConnection connection, final CompoundTag registry) {
        final ListTag<?> biomes = TagUtil.getRegistryEntries(registry, "worldgen/biome");
        tracker(connection).setBiomesSent(biomes.size());
    }

    public PacketHandler dimensionDataHandler() {
        return wrapper -> cacheDimensionData(wrapper.user(), wrapper.get(Types.NAMED_COMPOUND_TAG, 0));
    }

    public PacketHandler configurationDimensionDataHandler() {
        return wrapper -> cacheDimensionData(wrapper.user(), wrapper.get(Types.COMPOUND_TAG, 0));
    }

    /**
     * Caches dimension data, later used to get height values and other important info.
     */
    public void cacheDimensionData(final UserConnection connection, final CompoundTag registry) {
        final ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(registry, "dimension_type");
        final Map<String, DimensionData> dimensionDataMap = new HashMap<>(dimensions.size());
        for (final CompoundTag dimension : dimensions) {
            final NumberTag idTag = dimension.getNumberTag("id");
            final CompoundTag element = dimension.getCompoundTag("element");
            final String name = dimension.getStringTag("name").getValue();
            dimensionDataMap.put(Key.stripMinecraftNamespace(name), new DimensionDataImpl(idTag.asInt(), element));
        }
        tracker(connection).setDimensions(dimensionDataMap);
    }

    public EntityType trackAndRewrite(final PacketWrapper wrapper, final int typeId, final int entityId) {
        final int mappedTypeId = newEntityId(typeId);
        if (mappedTypeId != typeId) {
            wrapper.set(Types.VAR_INT, 1, mappedTypeId);
        }

        final EntityType entityType = typeFromId(trackMappedType ? mappedTypeId : typeId);
        if (entityType == null) {
            return null;
        }

        tracker(wrapper.user()).addEntity(entityId, entityType);
        return entityType;
    }

    // ---------------------------------------------------------------------------
    // Sub 1.14.1 methods

    /**
     * Returns a packethandler to track and rewrite an entity.
     *
     * @param dataType type of the entity data list
     * @return handler for tracking and rewriting entities
     */
    public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<EntityData>> dataType) {
        return wrapper -> {
            int entityId = wrapper.get(Types.VAR_INT, 0);
            int type = wrapper.get(Types.VAR_INT, 1);
            trackAndRewrite(wrapper, type, entityId);
            if (dataType != null) {
                handleEntityData(entityId, wrapper.get(dataType, 0), wrapper.user());
            }
        };
    }

    public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<EntityData>> dataType, EntityType entityType) {
        return wrapper -> {
            int entityId = wrapper.get(Types.VAR_INT, 0);
            // Register Type ID
            tracker(wrapper.user()).addEntity(entityId, entityType);

            if (dataType != null) {
                handleEntityData(entityId, wrapper.get(dataType, 0), wrapper.user());
            }
        };
    }

    /**
     * Returns a packethandler to track an object entity.
     *
     * @return handler for tracking and rewriting entities
     */
    public PacketHandler objectTrackerHandler() {
        return wrapper -> {
            int entityId = wrapper.get(Types.VAR_INT, 0);
            byte type = wrapper.get(Types.BYTE, 0);
            int data = wrapper.get(Types.INT, 0);

            EntityType entType = objectTypeFromId(type, data);
            if (entType == null) {
                return;
            }

            tracker(wrapper.user()).addEntity(entityId, entType);
        };
    }

    // ---------------------------------------------------------------------------

    private void logException(Exception e, @Nullable EntityType type, List<EntityData> entityDataList, EntityData entityData) {
        if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
            protocol.getLogger().severe("An error occurred in entity data handler " + this.getClass().getSimpleName()
                + " for " + (type != null ? type.name() : "untracked") + " entity type: " + entityData);
            protocol.getLogger().severe(entityDataList.stream().sorted(Comparator.comparingInt(EntityData::id))
                .map(EntityData::toString).collect(Collectors.joining("\n", "Full entity data: ", "")));
            protocol.getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }
}
