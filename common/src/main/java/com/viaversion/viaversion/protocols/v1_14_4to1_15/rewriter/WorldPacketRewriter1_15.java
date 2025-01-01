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
package com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter;

import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_14;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_15;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class WorldPacketRewriter1_15 {

    public static void register(Protocol1_14_4To1_15 protocol) {
        BlockRewriter<ClientboundPackets1_14_4> blockRewriter = BlockRewriter.for1_14(protocol);

        blockRewriter.registerBlockEvent(ClientboundPackets1_14_4.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_14_4.BLOCK_UPDATE);
        blockRewriter.registerChunkBlocksUpdate(ClientboundPackets1_14_4.CHUNK_BLOCKS_UPDATE);
        blockRewriter.registerBlockBreakAck(ClientboundPackets1_14_4.BLOCK_BREAK_ACK);
        blockRewriter.registerLevelChunk(ClientboundPackets1_14_4.LEVEL_CHUNK, ChunkType1_14.TYPE, ChunkType1_15.TYPE, (connection, chunk) -> {
            if (chunk.isFullChunk()) {
                int[] biomeData = chunk.getBiomeData();
                int[] newBiomeData = new int[1024];
                if (biomeData != null) {
                    // Now in 4x4x4 areas - take the biome of each "middle"
                    for (int i = 0; i < 4; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            int x = (j << 2) + 2;
                            int z = (i << 2) + 2;
                            int oldIndex = (z << 4 | x);
                            newBiomeData[i << 2 | j] = biomeData[oldIndex];
                        }
                    }
                    // ... and copy it to the new y layers
                    for (int i = 1; i < 64; ++i) {
                        System.arraycopy(newBiomeData, 0, newBiomeData, i * 16, 16);
                    }
                }

                chunk.setBiomeData(newBiomeData);
            }
        });

        blockRewriter.registerLevelEvent(ClientboundPackets1_14_4.LEVEL_EVENT, 1010, 2001);
        protocol.registerClientbound(ClientboundPackets1_14_4.LEVEL_PARTICLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(Types.FLOAT, Types.DOUBLE); // 2 - X
                map(Types.FLOAT, Types.DOUBLE); // 3 - Y
                map(Types.FLOAT, Types.DOUBLE); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    if (id == 3 || id == 23) {
                        int data = wrapper.passthrough(Types.VAR_INT);
                        wrapper.set(Types.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                    } else if (id == 32) {
                        protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                    }
                });
            }
        });
    }
}
