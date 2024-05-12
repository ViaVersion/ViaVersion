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

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.MathUtil;

public final class WorldPacketRewriter1_19 {

    public static void register(final Protocol1_18_2To1_19 protocol) {
        final BlockRewriter<ClientboundPackets1_18> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_18.BLOCK_EVENT);
        blockRewriter.registerBlockChange(ClientboundPackets1_18.BLOCK_UPDATE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_18.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerEffect(ClientboundPackets1_18.LEVEL_EVENT, 1010, 2001);

        protocol.cancelClientbound(ClientboundPackets1_18.BLOCK_BREAK_ACK);

        protocol.registerClientbound(ClientboundPackets1_18.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            Preconditions.checkArgument(tracker.biomesSent() != -1, "Biome count not set");
            Preconditions.checkArgument(tracker.currentWorldSectionHeight() != -1, "Section height not set");
            final ChunkType1_18 chunkType = new ChunkType1_18(tracker.currentWorldSectionHeight(),
                    MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().mappedSize()),
                    MathUtil.ceilLog2(tracker.biomesSent()));
            final Chunk chunk = wrapper.passthrough(chunkType);
            for (final ChunkSection section : chunk.getSections()) {
                final DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
                for (int i = 0; i < blockPalette.size(); i++) {
                    final int id = blockPalette.idByIndex(i);
                    blockPalette.setIdByIndex(i, protocol.getMappingData().getNewBlockStateId(id));
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19.SET_BEACON, wrapper -> {
            // Primary effect
            if (wrapper.read(Type.BOOLEAN)) {
                wrapper.passthrough(Type.VAR_INT);
            } else {
                wrapper.write(Type.VAR_INT, -1);
            }

            // Secondary effect
            if (wrapper.read(Type.BOOLEAN)) {
                wrapper.passthrough(Type.VAR_INT);
            } else {
                wrapper.write(Type.VAR_INT, -1);
            }
        });
    }
}
