/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.MathUtil;

public final class WorldPackets {

    public static void register(final Protocol1_19To1_18_2 protocol) {
        final BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);
        blockRewriter.registerBlockAction(ClientboundPackets1_18.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_18.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_18.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_18.ACKNOWLEDGE_PLAYER_DIGGING);
        blockRewriter.registerEffect(ClientboundPackets1_18.EFFECT, 1010, 2001);

        protocol.registerClientbound(ClientboundPackets1_18.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
                    final Chunk1_18Type chunkType = new Chunk1_18Type(tracker.currentWorldSectionHeight(),
                            MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().size()),
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
            }
        });
    }
}
