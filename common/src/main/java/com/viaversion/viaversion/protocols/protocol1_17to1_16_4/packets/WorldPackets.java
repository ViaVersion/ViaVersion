/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_16_2;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.types.Chunk1_17Type;
import com.viaversion.viaversion.rewriter.BlockRewriter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class WorldPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_16_2.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16_2.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_16_2.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16_2.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerClientbound(ClientboundPackets1_16_2.WORLD_BORDER, null, new PacketRemapper() {
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

                    wrapper.setId(packetType.getId());
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.UPDATE_LIGHT, new PacketRemapper() {
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

        protocol.registerClientbound(ClientboundPackets1_16_2.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    Chunk chunk = wrapper.read(new Chunk1_16_2Type());
                    if (!chunk.isFullChunk()) {
                        // All chunks are full chunk packets now (1.16 already stopped sending non-full chunks)
                        // Construct multi block change packets instead
                        // Height map updates are lost (unless we want to fully cache and resend entire chunks)
                        // Block entities are always empty for non-full chunks in Vanilla
                        writeMultiBlockChangePacket(wrapper, chunk);
                        wrapper.cancel();
                        return;
                    }

                    // Normal full chunk writing
                    wrapper.write(new Chunk1_17Type(chunk.getSections().length), chunk);

                    // 1.17 uses a bitset for the mask
                    chunk.setChunkMask(BitSet.valueOf(new long[]{chunk.getBitmask()}));

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

        protocol.registerClientbound(ClientboundPackets1_16_2.JOIN_GAME, new PacketRemapper() {
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

                    UserConnection user = wrapper.user();
                    user.getEntityTracker(Protocol1_17To1_16_4.class).addEntity(wrapper.get(Type.INT, 0), Entity1_17Types.PLAYER);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    CompoundTag dimensionData = wrapper.passthrough(Type.NBT);
                    addNewDimensionData(dimensionData);
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16_2.EFFECT, 1010, 2001);
    }

    private static void writeMultiBlockChangePacket(PacketWrapper wrapper, Chunk chunk) throws Exception {
        long chunkPosition = (chunk.getX() & 0x3FFFFFL) << 42;
        chunkPosition |= (chunk.getZ() & 0x3FFFFFL) << 20;

        ChunkSection[] sections = chunk.getSections();
        for (int chunkY = 0; chunkY < sections.length; chunkY++) {
            ChunkSection section = sections[chunkY];
            if (section == null) continue;

            PacketWrapper blockChangePacket = wrapper.create(ClientboundPackets1_17.MULTI_BLOCK_CHANGE);
            blockChangePacket.write(Type.LONG, chunkPosition | (chunkY & 0xFFFFFL));
            blockChangePacket.write(Type.BOOLEAN, true); // Suppress light updates

            //TODO this can be optimized
            BlockChangeRecord[] blockChangeRecords = new BlockChangeRecord[4096];
            int j = 0;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int blockStateId = Protocol1_17To1_16_4.MAPPINGS.getNewBlockStateId(section.getFlatBlock(x, y, z));
                        blockChangeRecords[j++] = new BlockChangeRecord1_16_2(x, y, z, blockStateId);
                    }
                }
            }

            blockChangePacket.write(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY, blockChangeRecords);
            blockChangePacket.send(Protocol1_17To1_16_4.class);
        }
    }

    private static void addNewDimensionData(CompoundTag tag) {
        tag.put("min_y", new IntTag(0));
        tag.put("height", new IntTag(256));
    }
}
