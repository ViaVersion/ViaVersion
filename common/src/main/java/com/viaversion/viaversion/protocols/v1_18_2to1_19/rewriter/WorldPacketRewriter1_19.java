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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class WorldPacketRewriter1_19 {

    public static void register(final Protocol1_18_2To1_19 protocol) {
        final BlockRewriter<ClientboundPackets1_18> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_18.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_18.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate(ClientboundPackets1_18.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent(ClientboundPackets1_18.LEVEL_EVENT, 1010, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_18.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_18::new);

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
