/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_16_2;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16_2;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_17;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class WorldPacketRewriter1_17 {

    public static void register(Protocol1_16_4To1_17 protocol) {
        BlockRewriter<ClientboundPackets1_16_2> blockRewriter = BlockRewriter.for1_14(protocol);

        blockRewriter.registerBlockEvent(ClientboundPackets1_16_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_16_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate(ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerBlockBreakAck(ClientboundPackets1_16_2.BLOCK_BREAK_ACK);

        protocol.registerClientbound(ClientboundPackets1_16_2.SET_BORDER, null, wrapper -> {
            // Border packet actions have been split into individual packets (the content hasn't changed)
            int type = wrapper.read(Types.VAR_INT);
            ClientboundPacketType packetType = switch (type) {
                case 0 -> ClientboundPackets1_17.SET_BORDER_SIZE;
                case 1 -> ClientboundPackets1_17.SET_BORDER_LERP_SIZE;
                case 2 -> ClientboundPackets1_17.SET_BORDER_CENTER;
                case 3 -> ClientboundPackets1_17.INITIALIZE_BORDER;
                case 4 -> ClientboundPackets1_17.SET_BORDER_WARNING_DELAY;
                case 5 -> ClientboundPackets1_17.SET_BORDER_WARNING_DISTANCE;
                default -> throw new IllegalArgumentException("Invalid world border type received: " + type);
            };

            wrapper.setPacketType(packetType);
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.LIGHT_UPDATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // x
                map(Types.VAR_INT); // y
                map(Types.BOOLEAN); // trust edges
                handler(wrapper -> {
                    int skyLightMask = wrapper.read(Types.VAR_INT);
                    int blockLightMask = wrapper.read(Types.VAR_INT);
                    // Now all written as a representation of BitSets
                    wrapper.write(Types.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(skyLightMask)); // Sky light mask
                    wrapper.write(Types.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(blockLightMask)); // Block light mask
                    wrapper.write(Types.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(wrapper.read(Types.VAR_INT))); // Empty sky light mask
                    wrapper.write(Types.LONG_ARRAY_PRIMITIVE, toBitSetLongArray(wrapper.read(Types.VAR_INT))); // Empty block light mask

                    writeLightArrays(wrapper, skyLightMask);
                    writeLightArrays(wrapper, blockLightMask);
                });
            }

            private void writeLightArrays(PacketWrapper wrapper, int bitMask) {
                List<byte[]> light = new ArrayList<>();
                for (int i = 0; i < 18; i++) {
                    if (isSet(bitMask, i)) {
                        light.add(wrapper.read(Types.BYTE_ARRAY_PRIMITIVE));
                    }
                }

                // Now needs the length of the bytearray-array
                wrapper.write(Types.VAR_INT, light.size());
                for (byte[] bytes : light) {
                    wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, bytes);
                }
            }

            private long[] toBitSetLongArray(int bitmask) {
                return new long[]{bitmask};
            }

            private boolean isSet(int mask, int i) {
                return (mask & (1 << i)) != 0;
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.LEVEL_CHUNK, wrapper -> {
            Chunk chunk = wrapper.read(ChunkType1_16_2.TYPE);
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
            wrapper.write(new ChunkType1_17(chunk.getSections().length), chunk);

            // 1.17 uses a bitset for the mask
            chunk.setChunkMask(BitSet.valueOf(new long[]{chunk.getBitmask()}));

            // Rewrite block state ids
            blockRewriter.handleChunk(chunk);
        });

        blockRewriter.registerLevelEvent(ClientboundPackets1_16_2.LEVEL_EVENT, 1010, 2001);
    }

    private static void writeMultiBlockChangePacket(PacketWrapper wrapper, Chunk chunk) {
        long chunkPosition = (chunk.getX() & 0x3FFFFFL) << 42;
        chunkPosition |= (chunk.getZ() & 0x3FFFFFL) << 20;

        ChunkSection[] sections = chunk.getSections();
        for (int chunkY = 0; chunkY < sections.length; chunkY++) {
            ChunkSection section = sections[chunkY];
            if (section == null) continue;

            PacketWrapper blockChangePacket = wrapper.create(ClientboundPackets1_17.SECTION_BLOCKS_UPDATE);
            blockChangePacket.write(Types.LONG, chunkPosition | (chunkY & 0xFFFFFL));
            blockChangePacket.write(Types.BOOLEAN, true); // Suppress light updates

            //TODO this can be optimized
            BlockChangeRecord[] blockChangeRecords = new BlockChangeRecord[4096];
            DataPalette palette = section.palette(PaletteType.BLOCKS);
            int j = 0;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int blockStateId = Protocol1_16_4To1_17.MAPPINGS.getNewBlockStateId(palette.idAt(x, y, z));
                        blockChangeRecords[j++] = new BlockChangeRecord1_16_2(x, y, z, blockStateId);
                    }
                }
            }

            blockChangePacket.write(Types.VAR_LONG_BLOCK_CHANGE_ARRAY, blockChangeRecords);
            blockChangePacket.send(Protocol1_16_4To1_17.class);
        }
    }
}
