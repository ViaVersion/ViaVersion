package us.myles.ViaVersion.protocols.protocol1_14_1to1_14.packets;

import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.protocols.protocol1_14_1to1_14.Protocol1_14_1To1_14;
import us.myles.ViaVersion.protocols.protocol1_14_1to1_14.metadata.MetadataRewriter1_14_1To1_14;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;

public class EntityPackets {

    public static void register(Protocol1_14_1To1_14 protocol) {
        MetadataRewriter1_14_1To1_14 metadataRewriter = protocol.get(MetadataRewriter1_14_1To1_14.class);

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
                map(Types1_14.METADATA_LIST); // 12 - Metadata

                handler(metadataRewriter.getTrackerAndRewriter(Types1_14.METADATA_LIST));
            }
        });

        metadataRewriter.registerEntityDestroy(ClientboundPackets1_14.DESTROY_ENTITIES);

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
                map(Types1_14.METADATA_LIST); // 7 - Metadata

                handler(metadataRewriter.getTrackerAndRewriter(Types1_14.METADATA_LIST, Entity1_14Types.PLAYER));
            }
        });

        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_14.ENTITY_METADATA, Types1_14.METADATA_LIST);
    }
}
