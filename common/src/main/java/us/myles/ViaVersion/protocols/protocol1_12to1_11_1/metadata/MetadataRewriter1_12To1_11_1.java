package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_12Types;
import us.myles.ViaVersion.api.entities.Entity1_12Types.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.BedRewriter;

import java.util.List;

public class MetadataRewriter1_12To1_11_1 extends MetadataRewriter<Entity1_12Types.EntityType> {

    @Override
    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) {
        if (metadata.getValue() instanceof Item) {
            // Apply rewrite
            BedRewriter.toClientItem((Item) metadata.getValue());
        }
        // Evocation Illager aggressive property became 13
        if (type.is(EntityType.EVOCATION_ILLAGER)) {
            if (metadata.getId() == 12) {
                metadata.setId(13);
            }
        }
    }
}
