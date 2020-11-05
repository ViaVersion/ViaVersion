package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.BiomeStorage;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.types.Chunk1_17Type;

public class WorldPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_16_2.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16_2.BLOCK_CHANGE);
        blockRewriter.registerMultiBlockChange(ClientboundPackets1_16_2.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16_2.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerOutgoing(ClientboundPackets1_16_2.UPDATE_LIGHT, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BOOLEAN);

                    wrapper.write(Type.VAR_LONG, wrapper.read(Type.VAR_INT).longValue()); // Sky mask
                    wrapper.write(Type.VAR_LONG, wrapper.read(Type.VAR_INT).longValue()); // Block mask
                    wrapper.write(Type.VAR_LONG, wrapper.read(Type.VAR_INT).longValue()); // Empty sky mask
                    wrapper.write(Type.VAR_LONG, wrapper.read(Type.VAR_INT).longValue()); // Empty block mask
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    Chunk chunk = wrapper.read(new Chunk1_16_2Type());
                    wrapper.write(new Chunk1_17Type(), chunk);

                    BiomeStorage biomeStorage = wrapper.user().get(BiomeStorage.class);
                    if (chunk.isFullChunk()) {
                        biomeStorage.setBiomes(chunk.getX(), chunk.getZ(), chunk.getBiomeData());
                    } else {
                        // Biomes always have to be sent now
                        int[] biomes = biomeStorage.getBiomes(chunk.getX(), chunk.getZ());
                        if (biomes != null) {
                            chunk.setBiomeData(biomes);
                        } else {
                            Via.getPlatform().getLogger().warning("Biome data not found for chunk at " + chunk.getX() + ", " + chunk.getZ());
                            chunk.setBiomeData(new int[0]);
                        }
                    }

                    for (int s = 0; s < 16; s++) {
                        ChunkSection section = chunk.getSections()[s];
                        if (section == null) continue;
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                            int old = section.getPaletteEntry(i);
                            section.setPaletteEntry(i, protocol.getMappingData().getNewBlockStateId(old));
                        }
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.UNLOAD_CHUNK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int x = wrapper.passthrough(Type.INT);
                    int z = wrapper.passthrough(Type.INT);
                    wrapper.user().get(BiomeStorage.class).clearBiomes(x, z);
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16_2.EFFECT, 1010, 2001);
    }
}
