package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_12Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.InventoryPackets;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void handleMetadata(int entityId, Entity1_12Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        // metadatas.clear();
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            // 1.13 changed item to flat item (no data)
            if (metadata.getMetaType().getType() == Type.ITEM) {
                metadata.setMetaType(MetaType1_13.Slot);
                InventoryPackets.toClient((Item) metadata.getValue());
            }
            // Handle other changes
            try {
                if (type.is(Entity1_12Types.EntityType.AREA_EFFECT_CLOUD)) {
                    if (metadata.getId() == 10 || metadata.getId() == 11) {
                        // TODO: AreaEffectCloud has lost 2 integers and gained "ef"
                        // Will be implemented when more info is known
                        metadatas.remove(metadata); // Remove
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
