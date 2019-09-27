package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.PacketWrapper;
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

import java.util.*;
import java.util.logging.Logger;

public abstract class MetadataRewriter<T extends Protocol> extends Rewriter<T> {
    private final Class<? extends EntityTracker> entityTrackerClass;

    protected MetadataRewriter(T protocol, Class<? extends EntityTracker> entityTrackerClass) {
        super(protocol);
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

    public void registerMetadataRewriter(int oldPacketId, int newPacketId, Type<List<Metadata>> oldMetaType, Type<List<Metadata>> newMetaType) {
        getProtocol().registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                if (oldMetaType != null) {
                    map(oldMetaType, newMetaType);
                } else {
                    map(newMetaType);
                }
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        List<Metadata> metadata = wrapper.get(newMetaType, 0);
                        handleMetadata(entityId, metadata, wrapper.user());
                    }
                });
            }
        });
    }

    public void registerMetadataRewriter(int oldPacketId, int newPacketId, Type<List<Metadata>> metaType) {
        registerMetadataRewriter(oldPacketId, newPacketId, null, metaType);
    }

    public void registerExtraTracker(int packetId, EntityType entityType, Type intType) {
        getProtocol().registerOutgoing(State.PLAY, packetId, packetId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(intType); // 0 - Entity id
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.user().get(entityTrackerClass).addEntity((int) wrapper.get(intType, 0), entityType);
                    }
                });
            }
        });
    }

    public void registerExtraTracker(int packetId, EntityType entityType) {
        registerExtraTracker(packetId, entityType, Type.VAR_INT);
    }

    public void registerEntityDestroy(int oldPacketId, int newPacketId) {
        getProtocol().registerOutgoing(State.PLAY, oldPacketId, newPacketId, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entity ids
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        EntityTracker entityTracker = wrapper.user().get(entityTrackerClass);
                        for (int entity : wrapper.get(Type.VAR_INT_ARRAY, 0)) {
                            entityTracker.removeEntity(entity);
                        }
                    }
                });
            }
        });
    }

    public void registerEntityDestroy(int packetId) {
        registerEntityDestroy(packetId, packetId);
    }

    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
    }

    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, Map<Integer, Metadata> metadataMap, UserConnection connection) throws Exception {
        handleMetadata(entityId, type, metadata, metadatas, connection);
    }
}
