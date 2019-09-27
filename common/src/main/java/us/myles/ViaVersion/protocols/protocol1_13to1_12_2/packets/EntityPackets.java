package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_12;
import us.myles.ViaVersion.api.type.types.version.Types1_13;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.metadata.MetadataRewriter1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker1_13;

import java.util.Optional;

public class EntityPackets {
    public static void register(final Protocol protocol) {
        MetadataRewriter1_13To1_12_2 metadataRewriter = protocol.get(MetadataRewriter1_13To1_12_2.class);

        // Spawn Object
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
                                int oldId = wrapper.get(Type.INT, 0);
                                int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                                wrapper.set(Type.INT, 0, WorldPackets.toNewId(combined));
                            }

                            // Fix ItemFrame hitbox
                            if (entType.is(Entity1_13Types.EntityType.ITEM_FRAME)) {
                                int data = wrapper.get(Type.INT, 0);

                                switch (data) {
                                    // South
                                    case 0:
                                        data = 3;
                                        break;
                                    // West
                                    case 1:
                                        data = 4;
                                        break;
                                    // North is the same
                                    // East
                                    case 3:
                                        data = 5;
                                        break;
                                }

                                wrapper.set(Type.INT, 0, data);

                                // Register Type ID
                                wrapper.user().get(EntityTracker1_13.class).addEntity(entityId, entType);
                            }
                        }
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
                map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int type = wrapper.get(Type.VAR_INT, 1);

                        Optional<Integer> optNewType = EntityTypeRewriter.getNewId(type);
                        type = optNewType.orElse(type);
                        Entity1_13Types.EntityType entType = Entity1_13Types.getTypeFromId(type, false);

                        wrapper.set(Type.VAR_INT, 1, type);


                        // Register Type ID
                        wrapper.user().get(EntityTracker1_13.class).addEntity(entityId, entType);

                        metadataRewriter.handleMetadata(entityId, wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
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
                map(Types1_12.METADATA_LIST, Types1_13.METADATA_LIST); // 7 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Entity1_13Types.EntityType entType = Entity1_13Types.EntityType.PLAYER;
                        // Register Type ID
                        wrapper.user().get(EntityTracker1_13.class).addEntity(entityId, entType);
                        metadataRewriter.handleMetadata(entityId, wrapper.get(Types1_13.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Destroy entities
        metadataRewriter.registerEntityDestroy(0x32, 0x35);

        // Metadata packet
        metadataRewriter.registerMetadataRewriter(0x3C, 0x3F, Types1_12.METADATA_LIST, Types1_13.METADATA_LIST);
    }
}
