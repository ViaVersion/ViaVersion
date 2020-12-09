package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.metadata;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_16_2Types;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_17;
import us.myles.ViaVersion.api.rewriters.MetadataRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.EntityTracker1_17;

import java.util.List;

public class MetadataRewriter1_17To1_16_4 extends MetadataRewriter {

    public MetadataRewriter1_17To1_16_4(Protocol1_17To1_16_4 protocol) {
        super(protocol, EntityTracker1_17.class);
    }

    @Override
    public void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        metadata.setMetaType(MetaType1_17.byId(metadata.getMetaType().getTypeID()));
        if (metadata.getMetaType() == MetaType1_17.Slot) {
            InventoryPackets.toClient((Item) metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_17.BlockID) {
            int data = (int) metadata.getValue();
            metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
        } else if (metadata.getMetaType() == MetaType1_17.PARTICLE) {
            Particle particle = (Particle) metadata.getValue();
            if (particle.getId() == 14) {
                // RGB is now encoded as doubles
                for (int i = 0; i < 3; i++) {
                    Particle.ParticleData data = particle.getArguments().get(i);
                    data.setValue(((Number) data.getValue()).doubleValue());
                    data.setType(Type.DOUBLE);
                }
            }
            rewriteParticle(particle);
        }

        if (type == null) return;

        if (type.isOrHasParent(Entity1_16_2Types.EntityType.ENTITY)) {
            if (metadata.getId() >= 7) {
                metadata.setId(metadata.getId() + 1); // Ticks frozen added with id 7
            }
        }
    }

    @Override
    protected EntityType getTypeFromId(int type) {
        return Entity1_16_2Types.getTypeFromId(type);
    }
}
