package us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {

    public static void handleMetadata(int entityId, Entity1_15Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                if (metadata.getMetaType() == MetaType1_14.Slot) {
                    InventoryPackets.toClient((Item) metadata.getValue());
                } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
                    // Convert to new block id
                    int data = (int) metadata.getValue();
                    metadata.setValue(Protocol1_15To1_14_4.getNewBlockStateId(data));
                }

                if (type == null) continue;

                // Metadata 12 added to abstract_living
                if (metadata.getId() > 11 && type.isOrHasParent(Entity1_15Types.EntityType.LIVINGENTITY)) {
                    metadata.setId(metadata.getId() + 1); //TODO is it 11 or 12? what is it for?
                }

                //TODO new boolean with id 17 for enderman?
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
