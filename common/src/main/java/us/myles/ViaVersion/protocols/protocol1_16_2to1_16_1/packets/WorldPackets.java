package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.packets;

import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.types.Chunk1_16Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {

    public static void register(Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14, Protocol1_16_2To1_16_1::getNewBlockStateId, Protocol1_16_2To1_16_1::getNewBlockId);

        blockRewriter.registerBlockAction(ClientboundPackets1_16.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16.BLOCK_CHANGE);
        blockRewriter.registerMultiBlockChange(ClientboundPackets1_16.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerOutgoing(ClientboundPackets1_16.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    Chunk chunk = wrapper.passthrough(new Chunk1_16Type(clientWorld));
                    for (int s = 0; s < 16; s++) {
                        ChunkSection section = chunk.getSections()[s];
                        if (section == null) continue;
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                            int old = section.getPaletteEntry(i);
                            section.setPaletteEntry(i, Protocol1_16_2To1_16_1.getNewBlockStateId(old));
                        }
                    }
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16.EFFECT, 1010, 2001, InventoryPackets::getNewItemId);
        blockRewriter.registerSpawnParticle(ClientboundPackets1_16.SPAWN_PARTICLE, 3, 23, 34,
                null, InventoryPackets::toClient, Type.FLAT_VAR_INT_ITEM, Type.DOUBLE);
    }
}
