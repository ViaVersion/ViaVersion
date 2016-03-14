package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import org.bukkit.entity.EntityType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.util.EntityUtil;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion2.api.remapper.PacketHandler;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.remapper.ValueTransformer;
import us.myles.ViaVersion2.api.type.Type;

public class SpawnPackets {
    public static ValueTransformer<Integer, Double> toNewDouble = new ValueTransformer<Integer, Double>(Type.DOUBLE) {
        @Override
        public Double transform(PacketWrapper wrapper, Integer inputValue) {
            return inputValue / 32D;
        }
    };

    public static void register(Protocol protocol) {
        // Spawn Object Packet
        protocol.registerOutgoing(State.PLAY, 0x0E, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                    }
                });
                map(Type.BYTE); // 2 - Type

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        int typeID = wrapper.get(Type.BYTE, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityUtil.getTypeFromID(typeID, true));
                    }
                });

                map(Type.INT, toNewDouble); // 3 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 4 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 5 - Z - Needs to be divide by 32

                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw

                map(Type.INT); // 8 - Data

                // Create last 3 shorts
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
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

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.EXPERIENCE_ORB);
                    }
                });

                map(Type.INT, toNewDouble); // 1 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 2 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 3 - Z - Needs to be divide by 32

                map(Type.INT); // 4 - Data
            }
        });

        // Spawn Global Entity Packet
        protocol.registerOutgoing(State.PLAY, 0x2C, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE); // 1 - Type
                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        // Currently only lightning uses this
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.LIGHTNING);
                    }
                });

                map(Type.INT, toNewDouble); // 2 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 3 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 4 - Z - Needs to be divide by 32
            }
        });

        // Spawn Mob Packet
        protocol.registerOutgoing(State.PLAY, 0x0F, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                    }
                });
                map(Type.UNSIGNED_BYTE); // 2 - Type

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        int typeID = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityUtil.getTypeFromID(typeID, false));
                    }
                });

                map(Type.INT, toNewDouble); // 3 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 4 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 5 - Z - Needs to be divide by 32

                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch

                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z

                // TODO Rewrite Metadata
                map(Protocol1_9TO1_8.METADATA_LIST);
            }
        });

        // Spawn Painting Packet
        protocol.registerOutgoing(State.PLAY, 0x10, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.PAINTING);
                    }
                });


                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
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

                map(Type.INT, toNewDouble); // 2 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 3 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 4 - Z - Needs to be divide by 32

                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                map(Type.SHORT, Type.NOTHING); // Current Item is discontinued

                // TODO Rewrite Metadata
                map(Protocol1_9TO1_8.METADATA_LIST);
            }
        });

        // Entity Destroy Packet
        protocol.registerOutgoing(State.PLAY, 0x13, 0x30, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entities to destroy

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        Integer[] entities = wrapper.get(Type.VAR_INT_ARRAY, 0);
                        for (Integer entity : entities) {
                            // EntityTracker
                            wrapper.user().get(EntityTracker.class).removeEntity(entity);
                            // TODO: When holograms added and bossbars, remove too
                        }
                    }
                });
            }
        });
    }
}
