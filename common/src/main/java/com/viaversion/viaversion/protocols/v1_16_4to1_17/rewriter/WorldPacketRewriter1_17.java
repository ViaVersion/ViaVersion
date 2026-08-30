/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16_2;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_17;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.storage.LegacyChunkSectionStorage;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.storage.LegacyChunkSectionStorage.SectionState;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.storage.ProtocolStorables1_17;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class WorldPacketRewriter1_17 {

    private static final BlockChangeRecord[] EMPTY_BLOCK_CHANGE_RECORDS = new BlockChangeRecord[0];

    public static void register(Protocol1_16_4To1_17 protocol) {
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
            ProtocolStorables1_17 storables = wrapper.user().storables(protocol);
            LegacyChunkSectionStorage sectionStorage = storables.legacyChunkSectionStorage();
            if (!chunk.isFullChunk()) {
                // All chunks are full chunk packets now (1.16 already stopped sending non-full chunks)
                // Map the complete section snapshots once, compare them with the client-side cache, and emit only actual changes.
                // Height map updates are lost (unless we want to fully cache and resend entire chunks)
                // Block entities are always empty for non-full chunks in Vanilla
                protocol.getBlockRewriter().handleChunk(chunk);
                sectionStorage.markActive();
                writeMultiBlockChangePackets(wrapper, chunk, sectionStorage);
                wrapper.cancel();
                return;
            }

            // Normal full chunk writing
            wrapper.write(new ChunkType1_17(chunk.getSections().length), chunk);

            // 1.17 uses a bitset for the mask
            chunk.setChunkMask(BitSet.valueOf(new long[]{chunk.getBitmask()}));

            // Rewrite block state ids
            protocol.getBlockRewriter().handleChunk(chunk);

            // Pre-1.16 servers still send partial chunk updates, so keep a small copy of each full chunk to diff the first one against.
            if (shouldCacheFullChunks(wrapper, sectionStorage)) {
                sectionStorage.storeFullChunk(chunk, airBlockStateId());
            }
        });

        registerBlockStateTracking(protocol);
    }

    private static void registerBlockStateTracking(Protocol1_16_4To1_17 protocol) {
        protocol.appendClientbound(ClientboundPackets1_16_2.BLOCK_UPDATE, wrapper -> {
            BlockPosition position = wrapper.get(Types.BLOCK_POSITION1_14, 0);
            int blockStateId = wrapper.get(Types.VAR_INT, 0);
            storage(wrapper, protocol).updateBlock(position, blockStateId);
        });

        protocol.appendClientbound(ClientboundPackets1_16_2.BLOCK_BREAK_ACK, wrapper -> {
            BlockPosition position = wrapper.get(Types.BLOCK_POSITION1_14, 0);
            int blockStateId = wrapper.get(Types.VAR_INT, 0);
            storage(wrapper, protocol).updateBlock(position, blockStateId);
        });

        protocol.appendClientbound(ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE, wrapper -> {
            long sectionPosition = wrapper.get(Types.LONG, 0);
            BlockChangeRecord[] records = wrapper.get(Types.VAR_LONG_BLOCK_CHANGE_ARRAY, 0);
            storage(wrapper, protocol).updateSection(sectionPosition, records);
        });

        protocol.appendClientbound(ClientboundPackets1_16_2.FORGET_LEVEL_CHUNK, wrapper -> {
            int chunkX = wrapper.passthrough(Types.INT);
            int chunkZ = wrapper.passthrough(Types.INT);
            storage(wrapper, protocol).unloadChunk(chunkX, chunkZ);
        });

        // Respawn/login replace the client world and invalidate every cached section snapshot.
        protocol.appendClientbound(ClientboundPackets1_16_2.LOGIN, wrapper -> storage(wrapper, protocol).clear());
        protocol.appendClientbound(ClientboundPackets1_16_2.RESPAWN, wrapper -> storage(wrapper, protocol).clear());
    }

    private static void writeMultiBlockChangePackets(PacketWrapper wrapper, Chunk chunk, LegacyChunkSectionStorage storage) {
        long chunkPosition = (chunk.getX() & 0x3FFFFFL) << 42;
        chunkPosition |= (chunk.getZ() & 0x3FFFFFL) << 20;

        ChunkSection[] sections = chunk.getSections();
        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            ChunkSection section = sections[sectionY];
            if (section == null) continue;

            DataPalette palette = section.palette(PaletteType.BLOCKS);
            SectionState previousState = storage.section(chunk.getX(), sectionY, chunk.getZ());
            BlockChangeRecord[] records = createBlockChangeRecords(previousState, palette);
            if (records.length != 0) {
                PacketWrapper blockChangePacket = wrapper.create(ClientboundPackets1_17.SECTION_BLOCKS_UPDATE);
                blockChangePacket.write(Types.LONG, chunkPosition | (sectionY & 0xFFFFFL));
                blockChangePacket.write(Types.BOOLEAN, true); // Suppress light updates
                blockChangePacket.write(Types.VAR_LONG_BLOCK_CHANGE_ARRAY, records);
                blockChangePacket.send(Protocol1_16_4To1_17.class);
            }

            // Replace instead of incrmentally mutating the snapshot. This removes stale palette entries accumulated
            // from ordinary block updates and keeps the cache compact.
            storage.storeSection(chunk.getX(), sectionY, chunk.getZ(), SectionState.fromPalette(palette));
        }
    }

    private static BlockChangeRecord[] createBlockChangeRecords(final @Nullable SectionState previousState, DataPalette currentPalette) {
        if (previousState == null) {
            BlockChangeRecord[] records = new BlockChangeRecord[ChunkSection.SIZE];
            for (int sectionIndex = 0; sectionIndex < ChunkSection.SIZE; sectionIndex++) {
                records[sectionIndex] = recordBlockChange(sectionIndex, currentPalette.idAt(sectionIndex));
            }
            return records;
        }

        int changedBlocks = 0;
        for (int sectionIndex = 0; sectionIndex < ChunkSection.SIZE; sectionIndex++) {
            if (previousState.idAt(sectionIndex) != currentPalette.idAt(sectionIndex)) {
                changedBlocks++;
            }
        }
        if (changedBlocks == 0) {
            return EMPTY_BLOCK_CHANGE_RECORDS;
        }

        BlockChangeRecord[] records = new BlockChangeRecord[changedBlocks];
        int recordIndex = 0;
        for (int sectionIndex = 0; sectionIndex < ChunkSection.SIZE; sectionIndex++) {
            int blockStateId = currentPalette.idAt(sectionIndex);
            if (previousState.idAt(sectionIndex) != blockStateId) {
                records[recordIndex++] = recordBlockChange(sectionIndex, blockStateId);
            }
        }
        return records;
    }

    private static BlockChangeRecord recordBlockChange(int sectionIndex, int blockStateId) {
        return new BlockChangeRecord1_16_2(ChunkSection.xFromIndex(sectionIndex), ChunkSection.yFromIndex(sectionIndex), ChunkSection.zFromIndex(sectionIndex), blockStateId);
    }

    private static boolean shouldCacheFullChunks(PacketWrapper wrapper, LegacyChunkSectionStorage storage) {
        ProtocolVersion serverVersion = wrapper.user().getProtocolInfo().serverProtocolVersion();
        return storage.isActive() || serverVersion.isKnown() && serverVersion.olderThan(ProtocolVersion.v1_16);
    }

    private static int airBlockStateId() {
        return Protocol1_16_4To1_17.MAPPINGS.getNewBlockStateId(0);
    }

    private static LegacyChunkSectionStorage storage(PacketWrapper wrapper, Protocol1_16_4To1_17 protocol) {
        ProtocolStorables1_17 storables = wrapper.user().storables(protocol);
        return storables.legacyChunkSectionStorage();
    }
}
