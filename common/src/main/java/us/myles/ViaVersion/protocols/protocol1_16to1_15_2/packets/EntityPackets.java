package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.metadata.MetadataRewriter1_16To1_15_2;

public class EntityPackets {

    public static void register(Protocol protocol) {
        MetadataRewriter1_16To1_15_2 metadataRewriter = protocol.get(MetadataRewriter1_16To1_15_2.class);

        // Spawn entity
        metadataRewriter.registerSpawnTrackerWithData(0x00, 0x00, Entity1_16Types.EntityType.FALLING_BLOCK, Protocol1_16To1_15_2::getNewBlockStateId);

        // Spawn mob packet
        metadataRewriter.registerTracker(0x03, 0x03);

        // Spawn player packet
        metadataRewriter.registerTracker(0x05, 0x05, Entity1_16Types.EntityType.PLAYER);

        // Metadata
        metadataRewriter.registerMetadataRewriter(0x44, 0x45, Types1_14.METADATA_LIST);

        // Entity Destroy
        metadataRewriter.registerEntityDestroy(0x38, 0x38);

        // Respawn
        metadataRewriter.registerRespawn(0x3B, 0x3B);

        // Join Game
        metadataRewriter.registerJoinGame(0x26, 0x26, Entity1_16Types.EntityType.PLAYER);

        // Entity Properties
        protocol.registerOutgoing(State.PLAY, 0x59, 0x59, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = wrapper.passthrough(Type.INT);
                    for (int i = 0; i < size; i++) {
                        // Attributes have been renamed and are now namespaced identifiers
                        String key = wrapper.read(Type.STRING);
                        String attributeIdentifier = MappingData.attributeMappings.get(key);
                        if (attributeIdentifier == null) {
                            attributeIdentifier = "minecraft:" + key;
                            if (!us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData.isValid1_13Channel(attributeIdentifier)) {
                                Via.getPlatform().getLogger().warning("Invalid attribute: " + key);
                                wrapper.read(Type.DOUBLE);
                                int modifierSize = wrapper.read(Type.VAR_INT);
                                for (int j = 0; j < modifierSize; j++) {
                                    wrapper.read(Type.UUID);
                                    wrapper.read(Type.DOUBLE);
                                    wrapper.read(Type.BYTE);
                                }
                                continue;
                            }
                        }

                        wrapper.write(Type.STRING, attributeIdentifier);

                        wrapper.passthrough(Type.DOUBLE);
                        int modifierSize = wrapper.passthrough(Type.VAR_INT);
                        for (int j = 0; j < modifierSize; j++) {
                            wrapper.passthrough(Type.UUID);
                            wrapper.passthrough(Type.DOUBLE);
                            wrapper.passthrough(Type.BYTE);
                        }
                    }
                });
            }
        });
    }
}
