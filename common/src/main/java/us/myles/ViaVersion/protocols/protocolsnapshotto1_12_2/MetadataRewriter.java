package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_12Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void handleMetadata(int entityId, Entity1_12Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {

        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                if (type.is(Entity1_12Types.EntityType.AREA_EFFECT_CLOUD)) {
                    if (metadata.getId() == 10 || metadata.getId() == 11) {
                        // TODO: AreaEffectCloud has lost 2 integers and gained "ef"
                        // Will be implemented when more info is known
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
