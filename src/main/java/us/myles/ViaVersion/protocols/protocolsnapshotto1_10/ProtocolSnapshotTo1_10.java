package us.myles.ViaVersion.protocols.protocolsnapshotto1_10;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_10.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_10.storage.EntityTracker;

public class ProtocolSnapshotTo1_10 extends Protocol {
    private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<Float, Short>(Type.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Float inputValue) throws Exception {
            return (short) (inputValue * 16);
        }
    };

    @Override
    protected void registerPackets() {
        InventoryPackets.register(this);

        // Spawn mob packet
        registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.UNSIGNED_BYTE, Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_9.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Change Type :)
                        int type = wrapper.get(Type.VAR_INT, 1);
                        type = MetadataRewriter.rewriteEntityType(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                        wrapper.set(Type.VAR_INT, 1, type);
                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).getClientEntityTypes().put(wrapper.get(Type.VAR_INT, 0), type);
                        MetadataRewriter.handleMetadata(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                    }
                });
            }
        });

        // Sound effect
        registerOutgoing(State.PLAY, 0x46, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.FLOAT); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);
                        id = getNewSoundId(id);

                        if (id == -1) // Removed
                            wrapper.cancel();
                        wrapper.set(Type.VAR_INT, 0, id);
                    }
                });
            }
        });

        // Collect item packet
        registerOutgoing(State.PLAY, 0x48, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Collected entity id
                map(Type.VAR_INT); // 1 - Collector entity id

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.VAR_INT, 1); // 2 - Pickup Count
                    }
                });
            }
        });

        // Metadata packet
        registerOutgoing(State.PLAY, 0x39, 0x39, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_9.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        if (!wrapper.user().get(EntityTracker.class).getClientEntityTypes().containsKey(entityId)) {
                            return;
                        }
                        int type = wrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
                        MetadataRewriter.handleMetadata(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                    }
                });
            }
        });

        // Destroy entities
        registerOutgoing(State.PLAY, 0x30, 0x30, new PacketRemapper() {
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

        /*
            INCOMING PACKETS
         */

        // Block placement
        registerIncoming(State.PLAY, 0x1C, 0x1C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Location
                map(Type.VAR_INT); // 1 - Face
                map(Type.VAR_INT); // 2 - Hand

                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
            }
        });
    }

    private int getNewSoundId(int id) { //TODO Make it better, suggestions are welcome. It's ugly and hardcoded now.
        if (id == 196) // Experience orb sound got removed
            return -1;

        int newId = id;
        if (id >= 85) // Hello shulker boxes
            newId += 2;
        if (id >= 194) // Hello evocation things
            newId += 8;
        if (id >= 196) // Rip the Experience orb touch sound :'(
            newId -= 1;
        if (id >= 269) // Hello Liama's
            newId += 8;
        if (id >= 370) // Hello Vex
            newId += 4;
        if (id >= 376) // Hello vindication
            newId += 3;
        if (id >= 427) // Hello empty bottle
            newId += 1;
        if (id > 441) // Hello item totem use
            newId += 1;
        return newId;
    }


    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
    }
}
