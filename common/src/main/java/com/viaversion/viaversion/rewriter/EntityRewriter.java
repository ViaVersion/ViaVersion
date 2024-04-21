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
package com.viaversion.viaversion.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.Int2IntMapMappings;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.rewriter.meta.MetaFilter;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEventImpl;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class EntityRewriter<C extends ClientboundPacketType, T extends Protocol<C, ?, ?, ?>>
    extends RewriterBase<T> implements com.viaversion.viaversion.api.rewriter.EntityRewriter<T> {
    private static final Metadata[] EMPTY_ARRAY = new Metadata[0];
    protected final List<MetaFilter> metadataFilters = new ArrayList<>();
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
     * Returns a meta filter builder.
     * <p>
     * Calling {@link com.viaversion.viaversion.rewriter.meta.MetaFilter.Builder#register()} will automatically register the filter on this rewriter.
     *
     * @return meta filter builder
     */
    public MetaFilter.Builder filter() {
        return new MetaFilter.Builder(this);
    }

    /**
     * Registers a metadata filter.
     * Note that {@link com.viaversion.viaversion.rewriter.meta.MetaFilter.Builder#register()} already calls this method.
     *
     * @param filter filter to register
     * @throws IllegalArgumentException if the filter is already registered
     */
    public void registerFilter(MetaFilter filter) {
        Preconditions.checkArgument(!metadataFilters.contains(filter));
        metadataFilters.add(filter);
    }

    @Override
    public void handleMetadata(final int entityId, final List<Metadata> metadataList, final UserConnection connection) {
        final TrackedEntity entity = tracker(connection).entity(entityId);
        final EntityType type = entity != null ? entity.entityType() : null;
        for (final Metadata metadata : metadataList.toArray(EMPTY_ARRAY)) { // Copy the list to allow mutation
            MetaHandlerEvent event = null;
            for (final MetaFilter filter : metadataFilters) {
                if (!filter.isFiltered(type, metadata)) {
                    continue;
                }
                if (event == null) {
                    // Instantiate lazily and share event instance
                    event = new MetaHandlerEventImpl(connection, entity, entityId, metadata, metadataList);
                }

                try {
                    filter.handler().handle(event, metadata);
                } catch (final Exception e) {
                    logException(e, type, metadataList, metadata);
                    metadataList.remove(metadata);
                    break;
                }

                if (event.cancelled()) {
                    // Remove meta, and break current filter loop
                    metadataList.remove(metadata);
                    break;
                }
            }

            if (event != null && event.hasExtraMeta()) {
                // Finally, add newly created meta
                metadataList.addAll(event.extraMeta());
            }
        }

        if (entity != null) {
            entity.sentMetadata(true);
        }
    }

    @Override
    public int newEntityId(int id) {
        return typeMappings != null ? typeMappings.getNewIdOrDefault(id, id) : id;
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
     * Maps entity ids based on the enum constant's names.
     *
     * @param oldTypes     entity types of the higher version
     * @param newTypeClass entity types enum class of the lower version
     * @param <E>          new enum entity type
     */
    public <E extends Enum<E> & EntityType> void mapTypes(EntityType[] oldTypes, Class<E> newTypeClass) {
        if (typeMappings == null) {
            typeMappings = Int2IntMapMappings.of();
        }
        for (EntityType oldType : oldTypes) {
            try {
                E newType = Enum.valueOf(newTypeClass, oldType.name());
                typeMappings.setNewId(oldType.getId(), newType.getId());
            } catch (IllegalArgumentException notFound) {
                if (!typeMappings.contains(oldType.getId())) {
                    Via.getPlatform().getLogger().warning("Could not find new entity type for " + oldType + "! " +
                        "Old type: " + oldType.getClass().getEnclosingClass().getSimpleName() + ", new type: " + newTypeClass.getEnclosingClass().getSimpleName());
                }
            }
        }
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
     * Registers a metadata handler to rewrite, item, block, and particle ids stored in metadata.
     *
     * @param itemType       item meta type if needed
     * @param blockStateType block state meta type if needed
     * @param particleType   particle meta type if needed
     */
    public void registerMetaTypeHandler(@Nullable MetaType itemType, @Nullable MetaType blockStateType, @Nullable MetaType particleType) {
        registerMetaTypeHandler(itemType, null, blockStateType, particleType, null);
    }

    /**
     * Registers a metadata handler to rewrite, item, block, and particle ids stored in metadata.
     *
     * @param itemType               item meta type if needed
     * @param blockStateType         block state meta type if needed
     * @param optionalBlockStateType optional block state meta type if needed
     * @param particleType           particle meta type if needed
     * @param particlesType          particles meta type if needed
     */
    public void registerMetaTypeHandler(@Nullable MetaType itemType, @Nullable MetaType blockStateType, @Nullable MetaType optionalBlockStateType, @Nullable MetaType particleType, @Nullable MetaType particlesType) {
        filter().handler((event, meta) -> {
            final MetaType type = meta.metaType();
            if (type == itemType) {
                meta.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), meta.value()));
            } else if (type == blockStateType) {
                int data = meta.value();
                meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
            } else if (type == optionalBlockStateType) {
                int data = meta.value();
                if (data != 0) {
                    meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
                }
            } else if (type == particleType) {
                rewriteParticle(event.user(), meta.value());
            } else if (type == particlesType) {
                final Particle[] particles = meta.value();
                for (final Particle particle : particles) {
                    rewriteParticle(event.user(), particle);
                }
            }
        });
    }

    public void registerTracker(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                handler(trackerHandler());
            }
        });
    }

    public void registerTrackerWithData(C packetType, EntityType fallingBlockType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data
                handler(trackerHandler());
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == fallingBlockType) {
                        wrapper.set(Type.INT, 0, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.INT, 0)));
                    }
                });
            }
        });
    }

    public void registerTrackerWithData1_19(C packetType, EntityType fallingBlockType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
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
                map(Type.BYTE); // Head yaw
                map(Type.VAR_INT); // Data
                handler(trackerHandler());
                handler(wrapper -> {
                    if (protocol.getMappingData() == null) {
                        return;
                    }

                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityType entityType = tracker(wrapper.user()).entityType(entityId);
                    if (entityType == fallingBlockType) {
                        wrapper.set(Type.VAR_INT, 2, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.VAR_INT, 2)));
                    }
                });
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
        registerTracker(packetType, entityType, Type.VAR_INT);
    }

    /**
     * Sub 1.17 method for entity remove packets.
     *
     * @param packetType remove entities packet type
     */
    public void registerRemoveEntities(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int[] entityIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
            EntityTracker entityTracker = tracker(wrapper.user());
            for (int entity : entityIds) {
                entityTracker.removeEntity(entity);
            }
        });
    }

    /**
     * 1.17+ method for entity remove packets.
     *
     * @param packetType remove entities packet type
     */
    public void registerRemoveEntity(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int entityId = wrapper.passthrough(Type.VAR_INT);
            tracker(wrapper.user()).removeEntity(entityId);
        });
    }

    public void registerMetadataRewriter(C packetType, @Nullable Type<List<Metadata>> oldMetaType, Type<List<Metadata>> newMetaType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                if (oldMetaType != null) {
                    map(oldMetaType, newMetaType);
                } else {
                    map(newMetaType);
                }
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    List<Metadata> metadata = wrapper.get(newMetaType, 0);
                    handleMetadata(entityId, metadata, wrapper.user());
                });
            }
        });
    }

    public void registerMetadataRewriter(C packetType, Type<List<Metadata>> metaType) {
        registerMetadataRewriter(packetType, null, metaType);
    }

    public PacketHandler trackerHandler() {
        return trackerAndRewriterHandler(null);
    }

    public PacketHandler playerTrackerHandler() {
        return wrapper -> {
            final EntityTracker tracker = tracker(wrapper.user());
            final int entityId = wrapper.get(Type.INT, 0);
            tracker.setClientEntityId(entityId);
            tracker.addEntity(entityId, tracker.playerType());
        };
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

            CompoundTag registryData = wrapper.get(Type.NAMED_COMPOUND_TAG, nbtIndex);
            NumberTag height = registryData.getNumberTag("height");
            if (height != null) {
                int blockHeight = height.asInt();
                tracker.setCurrentWorldSectionHeight(blockHeight >> 4);
            } else {
                Via.getPlatform().getLogger().warning("Height missing in dimension data: " + registryData);
            }

            NumberTag minY = registryData.getNumberTag("min_y");
            if (minY != null) {
                tracker.setCurrentMinY(minY.asInt());
            } else {
                Via.getPlatform().getLogger().warning("Min Y missing in dimension data: " + registryData);
            }

            String world = wrapper.get(Type.STRING, 0);
            if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
                tracker.clearEntities();
                tracker.trackClientEntity();
            }
            tracker.setCurrentWorld(world);
        };
    }

    public PacketHandler worldDataTrackerHandlerByKey() {
        return wrapper -> {
            EntityTracker tracker = tracker(wrapper.user());
            String dimensionKey = wrapper.get(Type.STRING, 0);
            DimensionData dimensionData = tracker.dimensionData(dimensionKey);
            if (dimensionData == null) {
                Via.getPlatform().getLogger().severe("Dimension data missing for dimension: " + dimensionKey + ", falling back to overworld");
                dimensionData = tracker.dimensionData("minecraft:overworld");
                Preconditions.checkNotNull(dimensionData, "Overworld data missing");
            }

            tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
            tracker.setCurrentMinY(dimensionData.minY());

            String world = wrapper.get(Type.STRING, 1);
            if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
                tracker.clearEntities();
                tracker.trackClientEntity();
            }
            tracker.setCurrentWorld(world);
        };
    }

    public PacketHandler worldDataTrackerHandlerByKey1_20_5(final int dimensionIdIndex) {
        return wrapper -> {
            EntityTracker tracker = tracker(wrapper.user());
            int dimensionId = wrapper.get(Type.VAR_INT, dimensionIdIndex);
            DimensionData dimensionData = tracker.dimensionData(dimensionId);
            if (dimensionData == null) {
                Via.getPlatform().getLogger().severe("Dimension data missing for dimension: " + dimensionId + ", falling back to overworld");
                dimensionData = tracker.dimensionData("minecraft:overworld");
                Preconditions.checkNotNull(dimensionData, "Overworld data missing");
            }

            tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
            tracker.setCurrentMinY(dimensionData.minY());

            String world = wrapper.get(Type.STRING, 0);
            if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
                tracker.clearEntities();
                tracker.trackClientEntity();
            }
            tracker.setCurrentWorld(world);
        };
    }

    public PacketHandler biomeSizeTracker() {
        return wrapper -> trackBiomeSize(wrapper.user(), wrapper.get(Type.NAMED_COMPOUND_TAG, 0));
    }

    public PacketHandler configurationBiomeSizeTracker() {
        return wrapper -> trackBiomeSize(wrapper.user(), wrapper.get(Type.COMPOUND_TAG, 0));
    }

    public void trackBiomeSize(final UserConnection connection, final CompoundTag registry) {
        final ListTag<?> biomes = TagUtil.getRegistryEntries(registry, "worldgen/biome");
        tracker(connection).setBiomesSent(biomes.size());
    }

    public PacketHandler dimensionDataHandler() {
        return wrapper -> cacheDimensionData(wrapper.user(), wrapper.get(Type.NAMED_COMPOUND_TAG, 0));
    }

    public PacketHandler configurationDimensionDataHandler() {
        return wrapper -> cacheDimensionData(wrapper.user(), wrapper.get(Type.COMPOUND_TAG, 0));
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

    public PacketHandler registryDataHandler1_20_5() {
        return wrapper -> {
            final String registryKey = Key.stripMinecraftNamespace(wrapper.get(Type.STRING, 0));
            if (registryKey.equals("worldgen/biome")) {
                final RegistryEntry[] entries = wrapper.get(Type.REGISTRY_ENTRY_ARRAY, 0);
                tracker(wrapper.user()).setBiomesSent(entries.length);
            } else if (registryKey.equals("dimension_type")) {
                final RegistryEntry[] entries = wrapper.get(Type.REGISTRY_ENTRY_ARRAY, 0);
                final Map<String, DimensionData> dimensionDataMap = new HashMap<>(entries.length);
                for (int i = 0; i < entries.length; i++) {
                    final RegistryEntry entry = entries[i];
                    dimensionDataMap.put(entry.key(), new DimensionDataImpl(i, (CompoundTag) entry.tag()));
                }
                tracker(wrapper.user()).setDimensions(dimensionDataMap);
            }
        };
    }

    // ---------------------------------------------------------------------------
    // Sub 1.14.1 methods

    /**
     * Returns a packethandler to track and rewrite an entity.
     *
     * @param metaType type of the metadata list
     * @return handler for tracking and rewriting entities
     */
    public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<Metadata>> metaType) {
        return wrapper -> {
            int entityId = wrapper.get(Type.VAR_INT, 0);
            int type = wrapper.get(Type.VAR_INT, 1);

            int newType = newEntityId(type);
            if (newType != type) {
                wrapper.set(Type.VAR_INT, 1, newType);
            }

            EntityType entType = typeFromId(trackMappedType ? newType : type);
            // Register Type ID
            tracker(wrapper.user()).addEntity(entityId, entType);

            if (metaType != null) {
                handleMetadata(entityId, wrapper.get(metaType, 0), wrapper.user());
            }
        };
    }

    public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<Metadata>> metaType, EntityType entityType) {
        return wrapper -> {
            int entityId = wrapper.get(Type.VAR_INT, 0);
            // Register Type ID
            tracker(wrapper.user()).addEntity(entityId, entityType);

            if (metaType != null) {
                handleMetadata(entityId, wrapper.get(metaType, 0), wrapper.user());
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
            int entityId = wrapper.get(Type.VAR_INT, 0);
            byte type = wrapper.get(Type.BYTE, 0);

            EntityType entType = objectTypeFromId(type);
            // Register Type ID
            tracker(wrapper.user()).addEntity(entityId, entType);
        };
    }

    // ---------------------------------------------------------------------------

    public void rewriteParticle(UserConnection connection, Particle particle) {
        ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
        int id = particle.id();
        if (mappings.isBlockParticle(id)) {
            Particle.ParticleData<Integer> data = particle.getArgument(0);
            data.setValue(protocol.getMappingData().getNewBlockStateId(data.getValue()));
        } else if (mappings.isItemParticle(id) && protocol.getItemRewriter() != null) {
            Particle.ParticleData<Item> data = particle.getArgument(0);
            ItemRewriter<?> itemRewriter = protocol.getItemRewriter();
            Item item = itemRewriter.handleItemToClient(connection, data.getValue());
            if (itemRewriter.mappedItemType() != null && itemRewriter.itemType() != itemRewriter.mappedItemType()) {
                // Replace the type
                particle.set(0, itemRewriter.mappedItemType(), item);
            } else {
                data.setValue(item);
            }
        }

        particle.setId(protocol.getMappingData().getNewParticleId(id));
    }

    public void rewriteParticle(PacketWrapper wrapper, Type<Particle> from, Type<Particle> to) throws Exception {
        final Particle particle = wrapper.read(from);
        rewriteParticle(wrapper.user(), particle);
        wrapper.write(to, particle);
    }

    private void logException(Exception e, @Nullable EntityType type, List<Metadata> metadataList, Metadata metadata) {
        if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
            Logger logger = Via.getPlatform().getLogger();
            logger.severe("An error occurred in metadata handler " + this.getClass().getSimpleName()
                + " for " + (type != null ? type.name() : "untracked") + " entity type: " + metadata);
            logger.severe(metadataList.stream().sorted(Comparator.comparingInt(Metadata::id))
                .map(Metadata::toString).collect(Collectors.joining("\n", "Full metadata: ", "")));
            logger.log(Level.SEVERE, "Error: ", e);
        }
    }
}
