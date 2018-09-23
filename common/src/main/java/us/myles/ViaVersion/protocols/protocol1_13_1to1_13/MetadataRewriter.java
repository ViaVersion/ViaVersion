package us.myles.ViaVersion.protocols.protocol1_13_1to1_13;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.entities.Entity1_13Types.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.InventoryPackets;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {

    public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                // 1.13 changed item to flat item (no data)
                if (metadata.getMetaType() == MetaType1_13.Slot) {
                    InventoryPackets.toClient((Item) metadata.getValue());
                } else if (metadata.getMetaType() == MetaType1_13.BlockID) {
                    // Convert to new block id
                    int data = (int) metadata.getValue();
                    metadata.setValue(Protocol1_13_1To1_13.getNewBlockStateId(data));
                }
                if (type == null) continue;
                if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
                    // New block format
                    int data = (int) metadata.getValue();
                    metadata.setValue(Protocol1_13_1To1_13.getNewBlockStateId(data));
                }
                if (type.isOrHasParent(EntityType.ABSTRACT_ARROW) && metadata.getId() >= 7) {
                    metadata.setId(metadata.getId() + 1); // New shooter UUID
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
