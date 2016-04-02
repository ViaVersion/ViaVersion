package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.NewType;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

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
                    public void write(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                    }
                });
                map(Type.BYTE); // 2 - Type

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
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
                        int data = wrapper.get(Type.INT, 0); // Data (1st Integer)

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

                // Handle potions
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        final int entityID = wrapper.get(Type.VAR_INT, 0);
                        final int data = wrapper.get(Type.INT, 0); // Data

                        int typeID = wrapper.get(Type.BYTE, 0);
                        if (EntityUtil.getTypeFromID(typeID, true) == EntityType.SPLASH_POTION) {
                            // Convert this to meta data, woo!
                            PacketWrapper metaPacket = wrapper.create(0x39, new ValueCreator() {
                                @Override
                                public void write(PacketWrapper wrapper) throws Exception {
                                    wrapper.write(Type.VAR_INT, entityID);
                                    List<Metadata> meta = new ArrayList<>();
                                    Item item = new Item((short) Material.POTION.getId(), (byte) 1, (short) data, null);
                                    ItemRewriter.toClient(item); // Rewrite so that it gets the right nbt

                                    Metadata potion = new Metadata(5, NewType.Slot.getTypeID(), Type.ITEM, item);
                                    meta.add(potion);
                                    wrapper.write(Protocol1_9TO1_8.METADATA_LIST, meta);
                                }
                            });
                            metaPacket.send();
                        }
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
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.EXPERIENCE_ORB);
                    }
                });

                map(Type.INT, toNewDouble); // 1 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 2 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 3 - Z - Needs to be divide by 32

                map(Type.SHORT); // 4 - Amount to spawn
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
                    public void handle(PacketWrapper wrapper) throws Exception {
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
                    public void write(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        wrapper.write(Type.UUID, tracker.getEntityUUID(entityID)); // 1 - UUID
                    }
                });
                map(Type.UNSIGNED_BYTE); // 2 - Type

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
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

                map(Protocol1_9TO1_8.METADATA_LIST);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        List<Metadata> metadataList = wrapper.get(Protocol1_9TO1_8.METADATA_LIST, 0);
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        if (tracker.getClientEntityTypes().containsKey(entityID)) {
                            MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityID), metadataList);
                        } else {
                            System.out.println("Unable to find entity for metadata, entity ID: " + entityID);
                            metadataList.clear();
                        }
                    }
                });
                // Handler for meta data
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        List<Metadata> metadataList = wrapper.get(Protocol1_9TO1_8.METADATA_LIST, 0);
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.handleMetadata(entityID, metadataList);
                    }
                });
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
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.PAINTING);
                    }
                });


                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
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

                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.PLAYER);
                    }
                });

                map(Type.INT, toNewDouble); // 2 - X - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 3 - Y - Needs to be divide by 32
                map(Type.INT, toNewDouble); // 4 - Z - Needs to be divide by 32

                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                handler(new PacketHandler() { //Handle discontinued player hand item
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short item = wrapper.read(Type.SHORT);
//                        System.out.println(item);
                        if (item != 0) {
                            PacketWrapper packet = new PacketWrapper(0x3C, null, wrapper.user());
                            packet.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
                            packet.write(Type.VAR_INT, 1);
                            packet.write(Type.ITEM, new Item(item, (byte) 1, (short) 0, null));
                            try {
                                packet.send();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                map(Protocol1_9TO1_8.METADATA_LIST);

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        List<Metadata> metadataList = wrapper.get(Protocol1_9TO1_8.METADATA_LIST, 0);
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        if (tracker.getClientEntityTypes().containsKey(entityID)) {
                            MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityID), metadataList);
                        } else {
                            System.out.println("Unable to find entity for metadata, entity ID: " + entityID);
                            metadataList.clear();
                        }
                    }
                });

                // Handler for meta data
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        List<Metadata> metadataList = wrapper.get(Protocol1_9TO1_8.METADATA_LIST, 0);
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.handleMetadata(entityID, metadataList);
                    }
                });
            }
        });

        // Entity Destroy Packet
        protocol.registerOutgoing(State.PLAY, 0x13, 0x30, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entities to destroy

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Integer[] entities = wrapper.get(Type.VAR_INT_ARRAY, 0);
                        for (Integer entity : entities) {
                            // EntityTracker
                            wrapper.user().get(EntityTracker.class).removeEntity(entity);
                        }
                    }
                });
            }
        });
    }
}
