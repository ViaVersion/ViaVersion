package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_13_2;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data.EntityTypeRewriter;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.metadata.MetadataRewriter1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;

import java.util.LinkedList;
import java.util.List;

public class EntityPackets {

    public static void register(final Protocol protocol) {
        MetadataRewriter1_14To1_13_2 metadataRewriter = protocol.get(MetadataRewriter1_14To1_13_2.class);

        // Spawn entity
        protocol.registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE, Type.VAR_INT); // 2 - Type
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
                        int typeId = wrapper.get(Type.VAR_INT, 1);

                        Entity1_13Types.EntityType type1_13 = Entity1_13Types.getTypeFromId(typeId, true);
                        typeId = EntityTypeRewriter.getNewId(type1_13.getId()).orElse(type1_13.getId());
                        Entity1_14Types.EntityType type1_14 = Entity1_14Types.getTypeFromId(typeId);

                        if (type1_14 != null) {
                            int data = wrapper.get(Type.INT, 0);
                            if (type1_14.is(Entity1_14Types.EntityType.FALLING_BLOCK)) {
                                wrapper.set(Type.INT, 0, Protocol1_14To1_13_2.getNewBlockStateId(data));
                            } else if (type1_14.is(Entity1_14Types.EntityType.MINECART)) {
                                // default is 0 = rideable minecart
                                switch (data) {
                                    case 1:
                                        typeId = Entity1_14Types.EntityType.CHEST_MINECART.getId();
                                        break;
                                    case 2:
                                        typeId = Entity1_14Types.EntityType.FURNACE_MINECART.getId();
                                        break;
                                    case 3:
                                        typeId = Entity1_14Types.EntityType.TNT_MINECART.getId();
                                        break;
                                    case 4:
                                        typeId = Entity1_14Types.EntityType.SPAWNER_MINECART.getId();
                                        break;
                                    case 5:
                                        typeId = Entity1_14Types.EntityType.HOPPER_MINECART.getId();
                                        break;
                                    case 6:
                                        typeId = Entity1_14Types.EntityType.COMMANDBLOCK_MINECART.getId();
                                        break;
                                }
                            } else if ((type1_14.is(Entity1_14Types.EntityType.ITEM) && data > 0)
                                    || type1_14.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ARROW)) {
                                if (type1_14.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ARROW)) {
                                    wrapper.set(Type.INT, 0, data - 1);
                                }
                                // send velocity in separate packet, 1.14 is now ignoring the velocity
                                PacketWrapper velocity = wrapper.create(0x45);
                                velocity.write(Type.VAR_INT, entityId);
                                velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 0));
                                velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 1));
                                velocity.write(Type.SHORT, wrapper.get(Type.SHORT, 2));
                                velocity.send(Protocol1_14To1_13_2.class);
                            }

                            // Register Type ID
                            wrapper.user().get(EntityTracker1_14.class).addEntity(entityId, type1_14);
                        }

                        wrapper.set(Type.VAR_INT, 1, typeId);
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
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int type = wrapper.get(Type.VAR_INT, 1);

                        type = EntityTypeRewriter.getNewId(type).orElse(type);
                        wrapper.set(Type.VAR_INT, 1, type);

                        Entity1_14Types.EntityType entType = Entity1_14Types.getTypeFromId(type);
                        // Register Type ID
                        wrapper.user().get(EntityTracker1_14.class).addEntity(entityId, entType);

                        metadataRewriter.handleMetadata(entityId, wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Spawn painting
        protocol.registerOutgoing(State.PLAY, 0x04, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.VAR_INT);
                map(Type.POSITION, Type.POSITION1_14);
                map(Type.BYTE);
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
                map(Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST); // 7 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Entity1_14Types.EntityType entType = Entity1_14Types.EntityType.PLAYER;
                        // Register Type ID
                        wrapper.user().get(EntityTracker1_14.class).addEntity(entityId, entType);
                        metadataRewriter.handleMetadata(entityId, wrapper.get(Types1_14.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Animation
        protocol.registerOutgoing(State.PLAY, 0x06, 0x06, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        short animation = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        if (animation == 2) {  //Leave bed
                            EntityTracker1_14 tracker = wrapper.user().get(EntityTracker1_14.class);
                            int entityId = wrapper.get(Type.VAR_INT, 0);
                            tracker.setSleeping(entityId, false);

                            PacketWrapper metadataPacket = wrapper.create(0x43);
                            metadataPacket.write(Type.VAR_INT, entityId);
                            List<Metadata> metadataList = new LinkedList<>();
                            if (tracker.getClientEntityId() != entityId) {
                                metadataList.add(new Metadata(6, MetaType1_14.Pose, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                            }
                            metadataList.add(new Metadata(12, MetaType1_14.OptPosition, null));
                            metadataPacket.write(Types1_14.METADATA_LIST, metadataList);
                            metadataPacket.send(Protocol1_14To1_13_2.class);
                        }
                    }
                });
            }
        });

        // Use bed
        protocol.registerOutgoing(State.PLAY, 0x33, 0x43, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        EntityTracker1_14 tracker = wrapper.user().get(EntityTracker1_14.class);
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        tracker.setSleeping(entityId, true);

                        Position position = wrapper.read(Type.POSITION);
                        List<Metadata> metadataList = new LinkedList<>();
                        metadataList.add(new Metadata(12, MetaType1_14.OptPosition, position));
                        if (tracker.getClientEntityId() != entityId) {
                            metadataList.add(new Metadata(6, MetaType1_14.Pose, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                        }
                        wrapper.write(Types1_14.METADATA_LIST, metadataList);
                    }
                });
            }
        });

        // Destroy entities
        metadataRewriter.registerEntityDestroy(0x35, 0x37);

        // Metadata packet
        metadataRewriter.registerMetadataRewriter(0x3F, 0x43, Types1_13_2.METADATA_LIST, Types1_14.METADATA_LIST);
    }
}
