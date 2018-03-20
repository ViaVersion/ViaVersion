package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.WorldPackets;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                // Handle new MetaTypes
                if (metadata.getMetaType().getTypeID() > 4)
                    metadata.setMetaType(MetaType1_13.byId(metadata.getMetaType().getTypeID() + 1));

                // Handle String -> Chat DisplayName
                if (metadata.getId() == 2) {
                    metadata.setMetaType(MetaType1_13.OptChat);
                    if (metadata.getValue() != null && !((String) metadata.getValue()).isEmpty()) {
                        metadata.setValue(Protocol1_9TO1_8.fixJson((String) metadata.getValue()));
                    } else {
                        metadata.setValue(null);
                    }
                }

                // 1.13 changed item to flat item (no data)
                if (metadata.getMetaType() == MetaType1_13.Slot) {
                    metadata.setMetaType(MetaType1_13.Slot);
                    InventoryPackets.toClient((Item) metadata.getValue());
                } else if (metadata.getMetaType() == MetaType1_13.BlockID) {
                    // Convert to new block id
                    metadata.setValue(WorldPackets.toNewId((int) metadata.getValue()));
                }

                // Skip type related changes when the type is null
                if (type == null)
                    continue;

                // Handle new colors
                if (type.is(Entity1_13Types.EntityType.WOLF) && metadata.getId() == 17) {
                    metadata.setValue(15 - (int) metadata.getValue());
                }

                // Handle new zombie meta (INDEX 15 - Boolean - Zombie is shaking while enabled)
                if (type.isOrHasParent(Entity1_13Types.EntityType.ZOMBIE)) {
                    if (metadata.getId() > 14)
                        metadata.setId(metadata.getId() + 1);
                }

                // Handle other changes
                if (type.is(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD)) {
                    if (metadata.getId() == 9 || metadata.getId() == 10 || metadata.getId() == 11) {
                        // TODO: AreaEffectCloud has lost 2 integers and gained "ef"
                        // Will be implemented when more info is known
                        metadatas.remove(metadata); // Remove
                    }
                }
                // TODO: Boat has changed
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
