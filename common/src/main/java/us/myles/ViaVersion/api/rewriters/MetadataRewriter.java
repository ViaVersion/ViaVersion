package us.myles.ViaVersion.api.rewriters;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.ParticleMappings;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.storage.EntityTracker;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;

import java.util.List;
import java.util.logging.Logger;

public abstract class MetadataRewriter {
    private static final Metadata[] EMPTY_ARRAY = new Metadata[0];
    private final Class<? extends EntityTracker> entityTrackerClass;
    protected final Protocol protocol;
    private Int2IntMap typeMapping;

    protected MetadataRewriter(Protocol protocol, Class<? extends EntityTracker> entityTrackerClass) {
        this.protocol = protocol;
        this.entityTrackerClass = entityTrackerClass;
        protocol.put(this);
    }

    public final void handleMetadata(int entityId, List<Metadata> metadatas, UserConnection connection) {
        EntityType type = connection.get(entityTrackerClass).getEntity(entityId);
        for (Metadata metadata : metadatas.toArray(EMPTY_ARRAY)) {
            try {
                handleMetadata(entityId, type, metadata, metadatas, connection);
            } catch (Exception e) {
                metadatas.remove(metadata);
                if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                    Logger logger = Via.getPlatform().getLogger();

                    logger.warning("An error occurred with entity metadata handler");
                    logger.warning("This is most likely down to one of your plugins sending bad datawatchers. Please test if this occurs without any plugins except ViaVersion before reporting it on GitHub");
                    logger.warning("Also make sure that all your plugins are compatible with your server version.");
                    logger.warning("Entity type: " + type);
                    logger.warning("Metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }
    }

    protected void rewriteParticle(Particle particle) {
        ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
        int id = particle.getId();
        if (id == mappings.getBlockId() || id == mappings.getFallingDustId()) {
            Particle.ParticleData data = particle.getArguments().get(0);
            data.setValue(protocol.getMappingData().getNewBlockStateId(data.get()));
        } else if (id == mappings.getItemId()) {
            Particle.ParticleData data = particle.getArguments().get(0);
            data.setValue(protocol.getMappingData().getNewItemId(data.get()));
        }

        particle.setId(protocol.getMappingData().getNewParticleId(id));
    }

    //TODO add respawn/join once they stop changing too much

    public void registerTracker(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                handler(getTracker());
            }
        });
    }

    public void registerSpawnTrackerWithData(ClientboundPacketType packetType, EntityType fallingBlockType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data
                handler(getTracker());
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityType entityType = wrapper.user().get(entityTrackerClass).getEntity(entityId);
                    if (entityType == fallingBlockType) {
                        wrapper.set(Type.INT, 0, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.INT, 0)));
                    }
                });
            }
        });
    }

    public void registerTracker(ClientboundPacketType packetType, EntityType entityType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    wrapper.user().get(entityTrackerClass).addEntity(entityId, entityType);
                });
            }
        });
    }

    public void registerEntityDestroy(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY_PRIMITIVE); // 0 - Entity ids
                handler(wrapper -> {
                    EntityTracker entityTracker = wrapper.user().get(entityTrackerClass);
                    for (int entity : wrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0)) {
                        entityTracker.removeEntity(entity);
                    }
                });
            }
        });
    }

    public void registerMetadataRewriter(ClientboundPacketType packetType, @Nullable Type<List<Metadata>> oldMetaType, Type<List<Metadata>> newMetaType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
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

    public void registerMetadataRewriter(ClientboundPacketType packetType, Type<List<Metadata>> metaType) {
        registerMetadataRewriter(packetType, null, metaType);
    }

    public <T extends Enum<T> & EntityType> void mapTypes(EntityType[] oldTypes, Class<T> newTypeClass) {
        if (typeMapping == null) {
            typeMapping = new Int2IntOpenHashMap(oldTypes.length, 1F);
            typeMapping.defaultReturnValue(-1);
        }
        for (EntityType oldType : oldTypes) {
            try {
                T newType = Enum.valueOf(newTypeClass, oldType.name());
                typeMapping.put(oldType.getId(), newType.getId());
            } catch (IllegalArgumentException notFound) {
                if (!typeMapping.containsKey(oldType.getId())) {
                    Via.getPlatform().getLogger().warning("Could not find new entity type for " + oldType + "! " +
                            "Old type: " + oldType.getClass().getEnclosingClass().getSimpleName() + ", new type: " + newTypeClass.getEnclosingClass().getSimpleName());
                }
            }
        }
    }

    public void mapType(EntityType oldType, EntityType newType) {
        if (typeMapping == null) {
            typeMapping = new Int2IntOpenHashMap();
            typeMapping.defaultReturnValue(-1);
        }
        typeMapping.put(oldType.getId(), newType.getId());
    }

    public PacketHandler getTracker() {
        return getTrackerAndRewriter(null);
    }

    // ---------------------------------------------------------------------------
    // Sub 1.14.1 methods

    /**
     * Returns a packethandler to track and rewrite an entity.
     *
     * @param metaType type of the metadata list
     * @return handler for tracking and rewriting entities
     */
    public PacketHandler getTrackerAndRewriter(@Nullable Type<List<Metadata>> metaType) {
        return wrapper -> {
            int entityId = wrapper.get(Type.VAR_INT, 0);
            int type = wrapper.get(Type.VAR_INT, 1);

            int newType = getNewEntityId(type);
            if (newType != type) {
                wrapper.set(Type.VAR_INT, 1, newType);
            }

            EntityType entType = getTypeFromId(newType);
            // Register Type ID
            wrapper.user().get(entityTrackerClass).addEntity(entityId, entType);

            if (metaType != null) {
                handleMetadata(entityId, wrapper.get(metaType, 0), wrapper.user());
            }
        };
    }

    public PacketHandler getTrackerAndRewriter(@Nullable Type<List<Metadata>> metaType, EntityType entityType) {
        return wrapper -> {
            int entityId = wrapper.get(Type.VAR_INT, 0);
            // Register Type ID
            wrapper.user().get(entityTrackerClass).addEntity(entityId, entityType);

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
    public PacketHandler getObjectTracker() {
        return wrapper -> {
            int entityId = wrapper.get(Type.VAR_INT, 0);
            byte type = wrapper.get(Type.BYTE, 0);

            EntityType entType = getObjectTypeFromId(type);
            // Register Type ID
            wrapper.user().get(entityTrackerClass).addEntity(entityId, entType);
        };
    }

    // ---------------------------------------------------------------------------

    protected abstract EntityType getTypeFromId(int type);

    /**
     * Returns the entity type from the given id.
     * From 1.14 and onwards, this is the same exact value as {@link #getTypeFromId(int)}.
     *
     * @param type entity type id
     * @return EntityType from id
     */
    protected EntityType getObjectTypeFromId(int type) {
        return getTypeFromId(type);
    }

    /**
     * Returns the mapped entitiy (or the same if it has not changed).
     *
     * @param oldId old entity id
     * @return mapped entity id
     */
    public int getNewEntityId(int oldId) {
        return typeMapping != null ? typeMapping.getOrDefault(oldId, oldId) : oldId;
    }

    /**
     * To be overridden to handle metadata.
     *
     * @param entityId   entity id
     * @param type       entity type, or null if not tracked
     * @param metadata   current metadata
     * @param metadatas  full, mutable list of metadata
     * @param connection user connection
     */
    protected abstract void handleMetadata(int entityId, @Nullable EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception;

    @Nullable
    protected Metadata getMetaByIndex(int index, List<Metadata> metadataList) {
        for (Metadata metadata : metadataList) {
            if (metadata.getId() == index) {
                return metadata;
            }
        }
        return null;
    }
}
