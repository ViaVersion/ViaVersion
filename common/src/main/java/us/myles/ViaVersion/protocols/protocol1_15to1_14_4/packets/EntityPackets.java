package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;

public class EntityPackets {

    public static void register(Protocol protocol) {
        // Spawn entity
        protocol.registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                // Track Entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.UUID);

                        int typeId = wrapper.read(Type.VAR_INT);
                        Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(getNewEntityId(typeId));
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);
                        wrapper.write(Type.VAR_INT, entityType.getId());
                    }
                });
            }
        });


        // Spawn mob packet
        protocol.registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.UUID);

                        int typeId = wrapper.read(Type.VAR_INT);
                        Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(getNewEntityId(typeId));
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);
                        wrapper.write(Type.VAR_INT, entityType.getId());

                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.SHORT);
                        wrapper.passthrough(Type.SHORT);
                        wrapper.passthrough(Type.SHORT);
                        wrapper.read(Types1_14.METADATA_LIST); // removed - probably sent in an update packet?
                    }
                });
            }
        });

        // Spawn player packet
        protocol.registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.UUID);

                        int typeId = wrapper.read(Type.VAR_INT);
                        Entity1_15Types.EntityType entityType = Entity1_15Types.getTypeFromId(getNewEntityId(typeId));
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entityType);
                        wrapper.write(Type.VAR_INT, entityType.getId());

                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.DOUBLE);
                        wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.BYTE);
                        wrapper.read(Types1_14.METADATA); // removed - probably sent in an update packet?
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
    }

    public static int getNewEntityId(int oldId) {
        return oldId >= 4 ? oldId + 1 : oldId; // 4 = bee
    }
}
