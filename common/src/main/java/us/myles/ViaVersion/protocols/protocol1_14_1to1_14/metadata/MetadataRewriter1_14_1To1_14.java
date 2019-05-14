package us.myles.ViaVersion.protocols.protocol1_14_1to1_14.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.entities.Entity1_14Types.EntityType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;

import java.util.List;

public class MetadataRewriter1_14_1To1_14 extends MetadataRewriter<Entity1_14Types.EntityType> {

    public void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection)  {
        if (type == null) return;

        if (type.is(EntityType.VILLAGER) || type.is(EntityType.WANDERING_TRADER)) {
            if (metadata.getId() >= 15) {
                metadata.setId(metadata.getId() + 1);
            }
        }
    }
}
