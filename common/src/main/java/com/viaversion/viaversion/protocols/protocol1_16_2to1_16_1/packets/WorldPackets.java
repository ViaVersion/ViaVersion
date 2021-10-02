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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_16_2;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.types.Chunk1_16_2Type;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.types.Chunk1_16Type;
import com.viaversion.viaversion.rewriter.BlockRewriter;

import java.util.ArrayList;
import java.util.List;

public class WorldPackets {

    private static final BlockChangeRecord[] EMPTY_RECORDS = new BlockChangeRecord[0];

    public static void register(Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_16.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_16.BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_16.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerClientbound(ClientboundPackets1_16.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    Chunk chunk = wrapper.read(new Chunk1_16Type());
                    wrapper.write(new Chunk1_16_2Type(), chunk);

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

        protocol.registerClientbound(ClientboundPackets1_16.MULTI_BLOCK_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel();

                    int chunkX = wrapper.read(Type.INT);
                    int chunkZ = wrapper.read(Type.INT);

                    long chunkPosition = 0;
                    chunkPosition |= (chunkX & 0x3FFFFFL) << 42;
                    chunkPosition |= (chunkZ & 0x3FFFFFL) << 20;

                    List<BlockChangeRecord>[] sectionRecords = new List[16];
                    BlockChangeRecord[] blockChangeRecord = wrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
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

                        PacketWrapper newPacket = wrapper.create(ClientboundPackets1_16_2.MULTI_BLOCK_CHANGE);
                        newPacket.write(Type.LONG, chunkPosition | (chunkY & 0xFFFFFL));
                        newPacket.write(Type.BOOLEAN, false); // Ignore light updates
                        newPacket.write(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY, sectionRecord.toArray(EMPTY_RECORDS));
                        newPacket.send(Protocol1_16_2To1_16_1.class);
                    }
                });
            }
        });

        blockRewriter.registerEffect(ClientboundPackets1_16.EFFECT, 1010, 2001);
    }
}
