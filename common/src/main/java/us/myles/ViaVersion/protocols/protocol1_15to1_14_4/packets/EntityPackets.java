package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.metadata.MetadataRewriter1_15To1_14_4;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker1_15;

import java.util.List;

public class EntityPackets {

    public static void register(Protocol1_15To1_14_4 protocol) {
        MetadataRewriter1_15To1_14_4 metadataRewriter = protocol.get(MetadataRewriter1_15To1_14_4.class);

        metadataRewriter.registerSpawnTrackerWithData(ClientboundPackets1_14.SPAWN_ENTITY, Entity1_15Types.EntityType.FALLING_BLOCK);

        protocol.registerOutgoing(ClientboundPackets1_14.SPAWN_MOB, new PacketRemapper() {
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

                handler(metadataRewriter.getTracker());
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    List<Metadata> metadata = wrapper.read(Types1_14.METADATA_LIST);
                    metadataRewriter.handleMetadata(entityId, metadata, wrapper.user());
                    PacketWrapper metadataUpdate = wrapper.create(0x44);
                    metadataUpdate.write(Type.VAR_INT, entityId);
                    metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                    metadataUpdate.send(Protocol1_15To1_14_4.class);
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_14.SPAWN_PLAYER, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch

                handler(wrapper -> {
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    Entity1_15Types.EntityType entityType = Entity1_15Types.EntityType.PLAYER;
                    wrapper.user().get(EntityTracker1_15.class).addEntity(entityId, entityType);

                    List<Metadata> metadata = wrapper.read(Types1_14.METADATA_LIST);
                    metadataRewriter.handleMetadata(entityId, metadata, wrapper.user());
                    PacketWrapper metadataUpdate = wrapper.create(0x44);
                    metadataUpdate.write(Type.VAR_INT, entityId);
                    metadataUpdate.write(Types1_14.METADATA_LIST, metadata);
                    metadataUpdate.send(Protocol1_15To1_14_4.class);
                });
            }
        });

        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_14.ENTITY_METADATA, Types1_14.METADATA_LIST);
        metadataRewriter.registerEntityDestroy(ClientboundPackets1_14.DESTROY_ENTITIES);
    }

    public static int getNewEntityId(int oldId) {
        return oldId >= 4 ? oldId + 1 : oldId; // 4 = bee
    }
}
