package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;

import java.util.List;
import java.util.UUID;

public class EntityPackets {

    public static void register(Protocol protocol) {
        // Spawn entity
        protocol.registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.VAR_INT); // 2 - Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                // Track Entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        UUID uuid = wrapper.get(Type.UUID, 0);
                        int typeId = wrapper.get(Type.VAR_INT, 1);

                        Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(getNewEntityId(typeId));
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);
                        wrapper.set(Type.VAR_INT, 1, entityType.getId());

                        if (entityType == Entity1_15Types.EntityType.FALLING_BLOCK) {
                            wrapper.set(Type.INT, 0, Protocol1_15To1_14_4.getNewBlockStateId(wrapper.get(Type.INT, 0)));
                        }
                    }
                });
            }
        });


        // Spawn mob packet
        protocol.registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int typeId = wrapper.get(Type.VAR_INT, 1);
                        Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(getNewEntityId(typeId));
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);
                        wrapper.set(Type.VAR_INT, 1, entityType.getId());

                        List<Metadata> metadata = wrapper.read(Types1_14.METADATA_LIST);
                        MetadataRewriter.handleMetadata(entityId, entityType, metadata, wrapper.user());
                        PacketWrapper metadataUpdate = wrapper.create(0x44);
                        metadataUpdate.write(Type.VAR_INT, entityId);
                        metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                        metadataUpdate.send(Protocol1_15To1_14_4.class);
                    }
                });
            }
        });

        // Spawn player packet
        protocol.registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        Entity1_15Types.EntityType entityType = Entity1_15Types.EntityType.PLAYER;
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);

                        List<Metadata> metadata = wrapper.read(Types1_14.METADATA_LIST);
                        MetadataRewriter.handleMetadata(entityId, entityType, metadata, wrapper.user());
                        PacketWrapper metadataUpdate = wrapper.create(0x44);
                        metadataUpdate.write(Type.VAR_INT, entityId);
                        metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                        metadataUpdate.send(Protocol1_15To1_14_4.class);
                    }
                });
            }
        });

        // Metadata packet
        protocol.registerOutgoing(State.PLAY, 0x43, 0x44, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_14.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        Optional<Entity1_15Types.EntityType> type = wrapper.user().get(EntityTracker.class).get(entityId);
                        MetadataRewriter.handleMetadata(entityId, type.orNull(), wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Destroy entities
        protocol.registerOutgoing(State.PLAY, 0x37, 0x38, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                        for (int entity : wrapper.get(Type.VAR_INT_ARRAY, 0)) {
                            entityTracker.removeEntity(entity);
                        }
                    }
                });
            }
        });
    }

    public static int getNewEntityId(int oldId) {
        return oldId >= 4 ? oldId + 1 : oldId; // 4 = bee
    }
}
