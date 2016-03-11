package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.type.Type;

public class SpawnPackets {
    public static void register(Protocol protocol) {
        // Spawn Object Packet
        protocol.registerOutgoing(State.PLAY, 0x0E, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                // 1 - UUID
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().object(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                    }
                });
                map(Type.BYTE); // 2 - Type

                map(Type.INT, Type.DOUBLE); // 3 - X - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 4 - Y - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 5 - Z - Needs to be divide by 32

                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw

                map(Type.INT); // 8 - Data

                // Create last 3 shorts
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int data = wrapper.get(Type.INT, 3); // Data (4th Integer)

                        short vX = 0, vY = 0, vZ = 0;
                        if (data > 0) {
                            vX = wrapper.read(Type.SHORT);
                            vY = wrapper.read(Type.SHORT);
                            vZ = wrapper.read(Type.SHORT);
                        }

                        wrapper.write(Type.SHORT, vX);
                        wrapper.write(Type.SHORT, vY);
                        wrapper.write(Type.SHORT, vZ);
                    }
                });

            }
        });

        // Spawn XP Packet
        protocol.registerOutgoing(State.PLAY, 0x11, 0x01, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                map(Type.INT, Type.DOUBLE); // 1 - X - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 2 - Y - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 3 - Z - Needs to be divide by 32

                map(Type.INT); // 4 - Data
            }
        });

        // Spawn Global Entity Packet
        protocol.registerOutgoing(State.PLAY, 0x2C, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE); // 1 - Type

                map(Type.INT, Type.DOUBLE); // 2 - X - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 3 - Y - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 4 - Z - Needs to be divide by 32
            }
        });

        // Spawn Mob Packet
        protocol.registerOutgoing(State.PLAY, 0x0F, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                // 1 - UUID
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().object(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                    }
                });
                map(Type.UNSIGNED_BYTE); // 2 - Type

                map(Type.INT, Type.DOUBLE); // 3 - X - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 4 - Y - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 5 - Z - Needs to be divide by 32

                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch

                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                // TODO Read Metadata
            }
        });

        // Spawn Painting Packet
        protocol.registerOutgoing(State.PLAY, 0x10, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                // 1 - UUID
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().object(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID));
                    }
                });
                map(Type.STRING); // 2 - Title

                map(Type.POSITION); // 3 - Position

                map(Type.BYTE); // 4 - Direction
            }
        });

        // Spawn Player Packet
        protocol.registerOutgoing(State.PLAY, 0x0C, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID

                map(Type.INT, Type.DOUBLE); // 2 - X - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 3 - Y - Needs to be divide by 32
                map(Type.INT, Type.DOUBLE); // 4 - Z - Needs to be divide by 32

                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                map(Type.SHORT, Type.NOTHING); // Current Item is discontinued

                // TODO Read Metadata
            }
        });
    }
}
