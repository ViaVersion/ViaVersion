package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.entities.Entity1_16_2Types;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.api.type.types.Particle;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.storage.EntityTracker1_16_2;

import java.util.List;

public class MetadataRewriter1_16_2To1_16_1 extends MetadataRewriter {

    public MetadataRewriter1_16_2To1_16_1(Protocol1_16_2To1_16_1 protocol) {
        super(protocol, EntityTracker1_16_2.class);
        mapTypes(Entity1_16Types.EntityType.values(), Entity1_16_2Types.EntityType.class);
    }

    @Override
    public void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        if (metadata.getMetaType() == MetaType1_14.Slot) {
            InventoryPackets.toClient((Item) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
            int data = (int) metadata.getValue();
            metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
        } else if (metadata.getMetaType() == MetaType1_14.PARTICLE) {
            rewriteParticle((Particle) metadata.getValue());
        }

        if (type == null) return;

        if (type.isOrHasParent(Entity1_16_2Types.EntityType.ABSTRACT_PIGLIN)) {
            if (metadata.getId() == 15) {
                metadata.setId(16);
            } else if (metadata.getId() == 16) {
                metadata.setId(15);
            }
        }
    }

    @Override
    protected EntityType getTypeFromId(int type) {
        return Entity1_16_2Types.getTypeFromId(type);
    }
}
