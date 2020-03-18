package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;

public class EntityPackets {

    public static void register(Protocol protocol) {
        MetadataRewriter1_16To1_15_2 metadataRewriter = protocol.get(MetadataRewriter1_16To1_15_2.class);

        // Spawn entity
        metadataRewriter.registerSpawnTrackerWithData(0x00, 0x00, Entity1_16Types.EntityType.FALLING_BLOCK, Protocol1_16To1_15_2::getNewBlockStateId);

        // Spawn mob packet
        metadataRewriter.registerTracker(0x03, 0x03);

        // Spawn player packet
        metadataRewriter.registerTracker(0x05, 0x05, Entity1_16Types.EntityType.PLAYER);

        // Metadata
        metadataRewriter.registerMetadataRewriter(0x44, 0x45, Types1_14.METADATA_LIST);

        // Entity Destroy
        metadataRewriter.registerEntityDestroy(0x38, 0x38);

        // Respawn
        metadataRewriter.registerRespawn(0x3B, 0x3B);

        // Join Game
        metadataRewriter.registerJoinGame(0x26, 0x26, Entity1_16Types.EntityType.PLAYER);
    }
}
