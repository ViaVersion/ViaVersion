package us.myles.ViaVersion.protocols.snapshot.protocol18w30ato1_13;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco Neuhaus on 28.07.2018 for the Project ViaVersion.
 */
public class MetadataRewriter {

    public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                if (metadata.getMetaType() == MetaType1_13.BlockID) {
                    // Convert to new block id
                    int data = (int) metadata.getValue();
                    if(data > 1126){
                        metadata.setValue(++data);
                    }
                }
                if(type == null) continue;
                if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
                    // New block format
                    int data = (int) metadata.getValue();
                    if(data > 1126){
                        metadata.setValue(++data);
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
