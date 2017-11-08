package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_12Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_12;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.EntityTracker;

// Development of 1.13 support!
public class ProtocolSnapshotTo1_12_2 extends Protocol {
    @Override
    protected void registerPackets() {
        // Outgoing packets
        // Spawn Object
        registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type

                // Track Entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {

                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        byte type = wrapper.get(Type.BYTE, 0);

                        Entity1_12Types.EntityType entType = Entity1_12Types.getTypeFromId(type, true);

                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                    }
                });
            }
        });

        // Spawn mob packet
        registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
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
                map(Types1_12.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        // Change Type :)
                        int type = wrapper.get(Type.VAR_INT, 1);

                        Entity1_12Types.EntityType entType = Entity1_12Types.getTypeFromId(type, false);
                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                        MetadataRewriter.handleMetadata(entityId, entType, wrapper.get(Types1_12.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0xF, 0xE);
        // 0xE Tab complete was removed
        registerOutgoing(State.PLAY, 0xE, 0xE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x10, 0xF);
        // New packet 0x10, empty packet, possible placeholder for new command system?
        // Destroy entities
        registerOutgoing(State.PLAY, 0x32, 0x32, new PacketRemapper() {
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
        registerOutgoing(State.PLAY, 0x3c, 0x3c, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_12.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Optional<Entity1_12Types.EntityType> type = wrapper.user().get(EntityTracker.class).get(entityId);
                        if (!type.isPresent())
                            return;

                        MetadataRewriter.handleMetadata(entityId, type.get(), wrapper.get(Types1_12.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });
        // 0x49 - New packet, options 0-4 followed by enum / string (possibly new command system?)
        registerOutgoing(State.PLAY, 0x49, 0x4A);
        registerOutgoing(State.PLAY, 0x4A, 0x4B);
        registerOutgoing(State.PLAY, 0x4B, 0x4C);
        registerOutgoing(State.PLAY, 0x4C, 0x4D);
        registerOutgoing(State.PLAY, 0x4D, 0x4E);
        registerOutgoing(State.PLAY, 0x4E, 0x4F);
        registerOutgoing(State.PLAY, 0x4F, 0x50);

        // Incoming packets
        registerIncoming(State.PLAY, 0x2, 0x1);
        // 0x1 Tab complete was removed
        registerIncoming(State.PLAY, 0x3, 0x2);
        registerIncoming(State.PLAY, 0x4, 0x3);
        registerIncoming(State.PLAY, 0x5, 0x4);
        registerIncoming(State.PLAY, 0x6, 0x5);
        registerIncoming(State.PLAY, 0x7, 0x6);
        registerIncoming(State.PLAY, 0x8, 0x7);
        registerIncoming(State.PLAY, 0x9, 0x8);
        registerIncoming(State.PLAY, 0xA, 0x9);
        registerIncoming(State.PLAY, 0xB, 0xA);
        registerIncoming(State.PLAY, 0xC, 0xB);
        registerIncoming(State.PLAY, 0xD, 0xC);
        registerIncoming(State.PLAY, 0xE, 0xD);
        registerIncoming(State.PLAY, 0xF, 0xE);
        registerIncoming(State.PLAY, 0x10, 0xF);
        registerIncoming(State.PLAY, 0x11, 0x10);
        registerIncoming(State.PLAY, 0x12, 0x11);
        registerIncoming(State.PLAY, 0x13, 0x12);
        registerIncoming(State.PLAY, 0x14, 0x13);
        registerIncoming(State.PLAY, 0x15, 0x14);
        registerIncoming(State.PLAY, 0x16, 0x15);
        registerIncoming(State.PLAY, 0x17, 0x16);
        registerIncoming(State.PLAY, 0x18, 0x17);
        registerIncoming(State.PLAY, 0x19, 0x18);
        registerIncoming(State.PLAY, 0x1A, 0x19);
        registerIncoming(State.PLAY, 0x1B, 0x1A);
        registerIncoming(State.PLAY, 0x1C, 0x1B);
        registerIncoming(State.PLAY, 0x1D, 0x1C);
        registerIncoming(State.PLAY, 0x1E, 0x1D);
        registerIncoming(State.PLAY, 0x1F, 0x1E);
        registerIncoming(State.PLAY, 0x20, 0x1F);

    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
    }
}
