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
package com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_16_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16_2;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import java.util.ArrayList;
import java.util.List;

public class WorldPacketRewriter1_16_2 {

    private static final BlockChangeRecord[] EMPTY_RECORDS = new BlockChangeRecord[0];

    public static void register(Protocol1_16_1To1_16_2 protocol) {
        BlockRewriter<ClientboundPackets1_16> blockRewriter = BlockRewriter.for1_14(protocol);

        blockRewriter.registerBlockEvent(ClientboundPackets1_16.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_16.BLOCK_UPDATE);
        blockRewriter.registerBlockBreakAck(ClientboundPackets1_16.BLOCK_BREAK_ACK);
        blockRewriter.registerLevelChunk(ClientboundPackets1_16.LEVEL_CHUNK, ChunkType1_16.TYPE, ChunkType1_16_2.TYPE);

        protocol.registerClientbound(ClientboundPackets1_16.CHUNK_BLOCKS_UPDATE, ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE, wrapper -> {
            wrapper.cancel();

            int chunkX = wrapper.read(Types.INT);
            int chunkZ = wrapper.read(Types.INT);

            long chunkPosition = 0;
            chunkPosition |= (chunkX & 0x3FFFFFL) << 42;
            chunkPosition |= (chunkZ & 0x3FFFFFL) << 20;

            List<BlockChangeRecord>[] sectionRecords = new List[16];
            BlockChangeRecord[] blockChangeRecord = wrapper.read(Types.BLOCK_CHANGE_ARRAY);
            for (BlockChangeRecord record : blockChangeRecord) {
                int chunkY = record.getY() >> 4;
                List<BlockChangeRecord> list = sectionRecords[chunkY];
                if (list == null) {
                    sectionRecords[chunkY] = (list = new ArrayList<>());
                }

                // Absolute y -> relative chunk section y
                int blockId = protocol.getMappingData().getNewBlockStateId(record.getBlockId());
                list.add(new BlockChangeRecord1_16_2(record.getSectionX(), record.getSectionY(), record.getSectionZ(), blockId));
            }

            // Now send separate packets for the different chunk sections
            for (int chunkY = 0; chunkY < sectionRecords.length; chunkY++) {
                List<BlockChangeRecord> sectionRecord = sectionRecords[chunkY];
                if (sectionRecord == null) continue;

                PacketWrapper newPacket = wrapper.create(ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE);
                newPacket.write(Types.LONG, chunkPosition | (chunkY & 0xFFFFFL));
                newPacket.write(Types.BOOLEAN, false); // Ignore light updates
                newPacket.write(Types.VAR_LONG_BLOCK_CHANGE_ARRAY, sectionRecord.toArray(EMPTY_RECORDS));
                newPacket.send(Protocol1_16_1To1_16_2.class);
            }
        });

        blockRewriter.registerLevelEvent(ClientboundPackets1_16.LEVEL_EVENT, 1010, 2001);
    }
}
