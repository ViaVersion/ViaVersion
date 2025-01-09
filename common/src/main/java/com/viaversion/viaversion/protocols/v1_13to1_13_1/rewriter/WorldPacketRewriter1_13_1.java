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
package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public class WorldPacketRewriter1_13_1 {

    public static void register(Protocol1_13To1_13_1 protocol) {
        BlockRewriter<ClientboundPackets1_13> blockRewriter = BlockRewriter.legacy(protocol);

        protocol.registerClientbound(ClientboundPackets1_13.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_13To1_13_1.class);
            Chunk chunk = wrapper.passthrough(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()));

            blockRewriter.handleChunk(chunk);
        });

        blockRewriter.registerBlockEvent(ClientboundPackets1_13.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_13.BLOCK_UPDATE);
        blockRewriter.registerChunkBlocksUpdate(ClientboundPackets1_13.CHUNK_BLOCKS_UPDATE);

        protocol.registerClientbound(ClientboundPackets1_13.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Effect Id
                map(Types.BLOCK_POSITION1_8); // Location
                map(Types.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    if (id == 2000) { // Smoke
                        int data = wrapper.get(Types.INT, 1);
                        switch (data) {
                            case 1: // North
                                wrapper.set(Types.INT, 1, 2); // North
                                break;
                            case 0: // North-West
                            case 3: // West
                            case 6: // South-West
                                wrapper.set(Types.INT, 1, 4); // West
                                break;
                            case 2: // North-East
                            case 5: // East
                            case 8: // South-East
                                wrapper.set(Types.INT, 1, 5); // East
                                break;
                            case 7: // South
                                wrapper.set(Types.INT, 1, 3); // South
                                break;
                            default: // Self and other directions
                                wrapper.set(Types.INT, 1, 0); // Down
                                break;
                        }
                    } else if (id == 1010) { // Play record
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(wrapper.get(Types.INT, 1)));
                    } else if (id == 2001) { // Block break + block break sound
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.INT, 1)));
                    }
                });
            }
        });
    }
}
