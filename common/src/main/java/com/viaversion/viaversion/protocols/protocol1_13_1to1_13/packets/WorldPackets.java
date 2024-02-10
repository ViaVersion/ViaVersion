/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13_1to1_13.packets;

import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public class WorldPackets {

    public static void register(Protocol1_13_1To1_13 protocol) {
        BlockRewriter<ClientboundPackets1_13> blockRewriter = BlockRewriter.legacy(protocol);

        protocol.registerClientbound(ClientboundPackets1_13.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            Chunk chunk = wrapper.passthrough(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()));

            for (ChunkSection section : chunk.getSections()) {
                if (section == null) {
                    continue;
                }

                DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int i = 0; i < palette.size(); i++) {
                    int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(palette.idByIndex(i));
                    palette.setIdByIndex(i, mappedBlockStateId);
                }
            }
        });

        blockRewriter.registerBlockAction(ClientboundPackets1_13.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_13.BLOCK_CHANGE);
        blockRewriter.registerMultiBlockChange(ClientboundPackets1_13.MULTI_BLOCK_CHANGE);

        protocol.registerClientbound(ClientboundPackets1_13.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Effect Id
                map(Type.POSITION1_8); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    if (id == 2000) { // Smoke
                        int data = wrapper.get(Type.INT, 1);
                        switch (data) {
                            case 1: // North
                                wrapper.set(Type.INT, 1, 2); // North
                                break;
                            case 0: // North-West
                            case 3: // West
                            case 6: // South-West
                                wrapper.set(Type.INT, 1, 4); // West
                                break;
                            case 2: // North-East
                            case 5: // East
                            case 8: // South-East
                                wrapper.set(Type.INT, 1, 5); // East
                                break;
                            case 7: // South
                                wrapper.set(Type.INT, 1, 3); // South
                                break;
                            default: // Self and other directions
                                wrapper.set(Type.INT, 1, 0); // Down
                                break;
                        }
                    } else if (id == 1010) { // Play record
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(wrapper.get(Type.INT, 1)));
                    } else if (id == 2001) { // Block break + block break sound
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.INT, 1)));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(wrapper -> {
                    // Store the player
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Dimension ID
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });
    }
}
