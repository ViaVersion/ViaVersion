package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_17Types;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.BiomeStorage;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.storage.EntityTracker1_17;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.types.Chunk1_17Type;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class WorldPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_16_2.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16_2.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_16_2.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16_2.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerOutgoing(ClientboundPackets1_16_2.WORLD_BORDER, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Border packet actions have been split into individual packets (the content hasn't changed)
                    int type = wrapper.read(Type.VAR_INT);
                    ClientboundPacketType packetType;
                    switch (type) {
                        case 0:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_SIZE;
                            break;
                        case 1:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_LERP_SIZE;
                            break;
                        case 2:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_CENTER;
                            break;
                        case 3:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_INIT;
                            break;
                        case 4:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_WARNING_DELAY;
                            break;
                        case 5:
                            packetType = ClientboundPackets1_17.WORLD_BORDER_WARNING_DISTANCE;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid world border type received: " + type);
                    }

                    wrapper.setId(packetType.ordinal());
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.UPDATE_LIGHT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // x
                map(Type.VAR_INT); // y
                map(Type.BOOLEAN); // trust edges
                handler(wrapper -> {
                    int skyLightMask = wrapper.read(Type.VAR_INT);
                    int blockLightMask = wrapper.read(Type.VAR_INT);
                    // Now all written as a representation of BitSets
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(skyLightMask)); // Sky light mask
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(blockLightMask)); // Block light mask
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(wrapper.read(Type.VAR_INT))); // Empty sky light mask
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(wrapper.read(Type.VAR_INT))); // Empty block light mask

                    writeLightArrays(wrapper, skyLightMask);
                    writeLightArrays(wrapper, blockLightMask);
                });
            }

            private void writeLightArrays(PacketWrapper wrapper, int bitMask) throws Exception {
                List<byte[]> light = new ArrayList<>();
                for (int i = 0; i <= 17; i++) {
                    if (isSet(bitMask, i)) {
                        light.add(wrapper.read(Type.BYTE_ARRAY_PRIMITIVE));
                    }
                }

                // Now needs the length of the bytearray-array
                wrapper.write(Type.VAR_INT, light.size());
                for (byte[] bytes : light) {
                    wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, bytes);
                }
            }

            private long[] toBitSetLongArray(int bitmask) {
                return new long[]{bitmask};
            }

            private boolean isSet(int mask, int i) {
                return (mask & (1 << i)) != 0;
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    Chunk chunk = wrapper.read(new Chunk1_16_2Type());
                    wrapper.write(new Chunk1_17Type(chunk.getSections().length), chunk);

                    // 1.17 uses a bitset for the mask
                    chunk.setChunkMask(BitSet.valueOf(new long[]{chunk.getBitmask()}));

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
                            chunk.setBiomeData(new int[chunk.getSections().length * 64]);
                        }
                    }

                    for (int s = 0; s < chunk.getSections().length; s++) {
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

        protocol.registerOutgoing(ClientboundPackets1_16_2.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Registry
                map(Type.NBT); // Current dimension
                handler(wrapper -> {
                    // Add new dimension fields
                    CompoundTag dimensionRegistry = wrapper.get(Type.NBT, 0).get("minecraft:dimension_type");
                    ListTag dimensions = dimensionRegistry.get("value");
                    for (Tag dimension : dimensions) {
                        CompoundTag dimensionCompound = ((CompoundTag) dimension).get("element");
                        addNewDimensionData(dimensionCompound);
                    }

                    CompoundTag currentDimensionTag = wrapper.get(Type.NBT, 1);
                    addNewDimensionData(currentDimensionTag);

                    // Tracking
                    String world = wrapper.passthrough(Type.STRING);

                    UserConnection user = wrapper.user();
                    user.get(BiomeStorage.class).setWorld(world);
                    user.get(EntityTracker1_17.class).addEntity(wrapper.get(Type.INT, 0), Entity1_17Types.PLAYER);
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    CompoundTag dimensionData = wrapper.passthrough(Type.NBT);
                    addNewDimensionData(dimensionData);

                    String world = wrapper.passthrough(Type.STRING);
                    BiomeStorage biomeStorage = wrapper.user().get(BiomeStorage.class);
                    if (!world.equals(biomeStorage.getWorld())) {
                        biomeStorage.clearBiomes();
                    }

                    biomeStorage.setWorld(world);
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

    private static void addNewDimensionData(CompoundTag tag) {
        tag.put("min_y", new IntTag(0));
        tag.put("height", new IntTag(256));
    }
}
