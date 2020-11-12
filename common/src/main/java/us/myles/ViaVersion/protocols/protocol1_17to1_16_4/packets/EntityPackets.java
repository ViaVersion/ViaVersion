package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets;

import us.myles.ViaVersion.api.entities.Entity1_16_2Types;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.metadata.MetadataRewriter1_17To1_16_4;

public class EntityPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        MetadataRewriter1_17To1_16_4 metadataRewriter = protocol.get(MetadataRewriter1_17To1_16_4.class);
        metadataRewriter.registerSpawnTrackerWithData(ClientboundPackets1_16_2.SPAWN_ENTITY, Entity1_16_2Types.EntityType.FALLING_BLOCK);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_PLAYER, Entity1_16_2Types.EntityType.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_16_2.ENTITY_METADATA, Types1_14.METADATA_LIST);
        metadataRewriter.registerEntityDestroy(ClientboundPackets1_16_2.DESTROY_ENTITIES);
    }
}
