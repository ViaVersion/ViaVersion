package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.storage.EntityTracker;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.*;
import java.util.logging.Logger;

public abstract class MetadataRewriter {
    private final Class<? extends EntityTracker> entityTrackerClass;
    private final Protocol protocol;

    protected MetadataRewriter(Protocol protocol, Class<? extends EntityTracker> entityTrackerClass) {
        this.protocol = protocol;
        this.entityTrackerClass = entityTrackerClass;
        protocol.put(this);
    }

    public final void handleMetadata(int entityId, List<Metadata> metadatas, UserConnection connection) {
        EntityType type = connection.get(entityTrackerClass).getEntity(entityId);
        Map<Integer, Metadata> metadataMap = new HashMap<>(metadatas.size());
        for (Metadata metadata : metadatas) {
            metadataMap.put(metadata.getId(), metadata);
        }

        metadataMap = Collections.unmodifiableMap(metadataMap);

        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                handleMetadata(entityId, type, metadata, metadatas, metadataMap, connection);
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

    public void registerJoinGame(int oldPacketId, int newPacketId, EntityType playerType) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientChunks.setEnvironment(dimensionId);

                    if (playerType != null) {
                        wrapper.user().get(entityTrackerClass).addEntity(wrapper.get(Type.INT, 0), playerType);
                    }
                });
            }
        });
    }

    public void registerRespawn(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });
    }

    public void registerTracker(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                handler(getTracker());
            }
        });
    }

    public void registerSpawnTrackerWithData(int oldPacketId, int newPacketId, EntityType fallingBlockType, IdRewriteFunction itemRewriter) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
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
                        wrapper.set(Type.INT, 0, itemRewriter.rewrite(wrapper.get(Type.INT, 0)));
                    }
                });
            }
        });
    }

    public void registerTracker(int oldPacketId, int newPacketId, EntityType entityType) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
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

    public void registerEntityDestroy(int oldPacketId, int newPacketId) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
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

    public void registerMetadataRewriter(int oldPacketId, int newPacketId, Type<List<Metadata>> oldMetaType, Type<List<Metadata>> newMetaType) {
        protocol.registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
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

    public void registerMetadataRewriter(int oldPacketId, int newPacketId, Type<List<Metadata>> metaType) {
        registerMetadataRewriter(oldPacketId, newPacketId, null, metaType);
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
    public PacketHandler getTrackerAndRewriter(Type<List<Metadata>> metaType) {
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

    public PacketHandler getTrackerAndRewriter(Type<List<Metadata>> metaType, EntityType entityType) {
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

    protected abstract EntityType getTypeFromId(int type);

    protected EntityType getObjectTypeFromId(int type) {
        return getTypeFromId(type);
    }

    protected int getNewEntityId(int oldId) {
        return oldId;
    }

    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
    }

    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, Map<Integer, Metadata> metadataMap, UserConnection connection) throws Exception {
        handleMetadata(entityId, type, metadata, metadatas, connection);
    }
}
