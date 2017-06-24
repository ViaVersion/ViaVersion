package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_12Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void handleMetadata(int entityId, Entity1_12Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                if (metadata.getValue() instanceof Item) {
                    // Apply rewrite
                    BedRewriter.toClientItem((Item) metadata.getValue());
                }
                // Evocation Illager aggressive property became 13
                if (type.is(Entity1_12Types.EntityType.EVOCATION_ILLAGER)) {
                    if (metadata.getId() == 12) {
                        metadata.setId(13);
                    }
                }
            } catch (Exception e) {
                metadatas.remove(metadata);
                if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("An error occurred with entity metadata handler");
                    Via.getPlatform().getLogger().warning("Metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }

    }
}
