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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class WorldPacketRewriter1_19 {

    public static void register(final Protocol1_18_2To1_19 protocol) {
        final BlockRewriter<ClientboundPackets1_18> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_18.BLOCK_EVENT);
        blockRewriter.registerLevelEvent(ClientboundPackets1_18.LEVEL_EVENT, 1010, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_18.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_18::new);

        protocol.registerClientbound(ClientboundPackets1_18.BLOCK_UPDATE, wrapper -> {
            final BlockPosition position = wrapper.passthrough(Types.BLOCK_POSITION1_14);
            wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewBlockStateId(wrapper.read(Types.VAR_INT)));
            Via.getManager().getProviders().get(AckSequenceProvider.class).handleBlockChange(wrapper.user(), position);
        });
        protocol.registerClientbound(ClientboundPackets1_18.SECTION_BLOCKS_UPDATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.LONG); // Chunk position
                map(Types.BOOLEAN); // Suppress light updates
                handler(wrapper -> {
                    final AckSequenceProvider ackSequenceProvider = Via.getManager().getProviders().get(AckSequenceProvider.class);
                    final long chunkPosition = wrapper.get(Types.LONG, 0);
                    final int x = (int) ((chunkPosition >> 42) & 0x3FFFFFL) << 4;
                    final int z = (int) ((chunkPosition >> 20) & 0x3FFFFFL) << 4;
                    final int y = (int) (chunkPosition & 0xFFFL) << 4;

                    for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                        final BlockPosition position = new BlockPosition(x + record.getSectionX(), y + record.getSectionY(), z + record.getSectionZ());
                        ackSequenceProvider.handleBlockChange(wrapper.user(), position);
                    }
                });
            }
        });

        protocol.cancelClientbound(ClientboundPackets1_18.BLOCK_BREAK_ACK);

        protocol.registerServerbound(ServerboundPackets1_19.SET_BEACON, wrapper -> {
            // Primary effect
            if (wrapper.read(Types.BOOLEAN)) {
                wrapper.passthrough(Types.VAR_INT);
            } else {
                wrapper.write(Types.VAR_INT, -1);
            }

            // Secondary effect
            if (wrapper.read(Types.BOOLEAN)) {
                wrapper.passthrough(Types.VAR_INT);
            } else {
                wrapper.write(Types.VAR_INT, -1);
            }
        });
    }
}
