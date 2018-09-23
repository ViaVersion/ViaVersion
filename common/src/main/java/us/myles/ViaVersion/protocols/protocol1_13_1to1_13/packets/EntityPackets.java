package us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.entities.Entity1_13Types.EntityType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_13;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;

public class EntityPackets {

    public static void register(Protocol protocol) {

        //spawn entity
        protocol.registerOutgoing(State.PLAY, 0x0, 0x0, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data

                // Track Entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        byte type = wrapper.get(Type.BYTE, 0);
                        Entity1_13Types.EntityType entType = Entity1_13Types.getTypeFromId(type, true);

                        if (entType != null) {
                            if (entType.is(Entity1_13Types.EntityType.FALLING_BLOCK)) {
                                int data = wrapper.get(Type.INT, 0);
                                wrapper.set(Type.INT, 0, Protocol1_13_1To1_13.getNewBlockStateId(data));
                            }
                        }
                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                    }
                });
            }
        });
        // Spawn mob packet
        protocol.registerOutgoing(State.PLAY, 0x3, 0x3, new PacketRemapper() {
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
                map(Types1_13.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int type = wrapper.get(Type.VAR_INT, 1);

                        Entity1_13Types.EntityType entType = Entity1_13Types.getTypeFromId(type, false);

                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);

                        MetadataRewriter.handleMetadata(entityId, entType, wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
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
                map(Types1_13.METADATA_LIST); // 7 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Entity1_13Types.EntityType entType = Entity1_13Types.EntityType.PLAYER;
                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                        MetadataRewriter.handleMetadata(entityId, entType, wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });
        // Destroy entities
        protocol.registerOutgoing(State.PLAY, 0x35, 0x35, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entity IDS

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        for (int entity : wrapper.get(Type.VAR_INT_ARRAY, 0))
                            wrapper.user().get(EntityTracker.class).removeEntity(entity);
                    }
                });
            }
        });


        // Metadata packet
        protocol.registerOutgoing(State.PLAY, 0x3F, 0x3F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_13.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Optional<EntityType> type = wrapper.user().get(EntityTracker.class).get(entityId);
                        MetadataRewriter.handleMetadata(entityId, type.orNull(), wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });
    }

}
