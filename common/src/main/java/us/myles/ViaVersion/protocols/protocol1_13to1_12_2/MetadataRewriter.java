package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        int particleId = -1, parameter1 = 0, parameter2 = 0;
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                // Handle new MetaTypes
                if (metadata.getMetaType().getTypeID() > 4)
                    metadata.setMetaType(MetaType1_13.byId(metadata.getMetaType().getTypeID() + 1));

                // Handle String -> Chat DisplayName
                if (metadata.getId() == 2) {
                    metadata.setMetaType(MetaType1_13.OptChat);
                    if (metadata.getValue() != null && !((String) metadata.getValue()).isEmpty()) {
                        metadata.setValue(ChatRewriter.legacyTextToJson((String) metadata.getValue()));
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

                // Handle Minecart inner block
                if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
                    // New block format
                    int oldId = (int) metadata.getValue();
                    int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                    int newId = WorldPackets.toNewId(combined);
                    metadata.setValue(newId);
                }

                // Handle other changes
                if (type.is(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD)) {
                    if (metadata.getId() == 9) {
                        particleId = (int) metadata.getValue();
                    } else if (metadata.getId() == 10) {
                        parameter1 = (int) metadata.getValue();
                    } else if (metadata.getId() == 11) {
                        parameter2 = (int) metadata.getValue();
                    }

                    if (metadata.getId() >= 9)
                        metadatas.remove(metadata); // Remove
                }

                if (metadata.getId() == 0) {
                    metadata.setValue((byte) ((byte) metadata.getValue() & ~0x10)); // Previously unused, now swimming
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

        // Handle AreaEffectCloud outside the loop
        if (type != null && type.is(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD) && particleId != -1) {
            Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
            metadatas.add(new Metadata(9, MetaType1_13.PARTICLE, particle));
        }
    }
}
