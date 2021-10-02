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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_17_1to1_17.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.types.Chunk1_17Type;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.BlockEntityIds;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.Protocol1_18To1_17_1;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.storage.ChunkLightStorage;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public final class WorldPackets {

    private static final int WIDTH_BITS = 2;
    private static final int HORIZONTAL_MASK = 3;
    private static final int BIOMES_PER_CHUNK = 4 * 4 * 4;

    public static void register(final Protocol1_18To1_17_1 protocol) {
        protocol.registerClientbound(ClientboundPackets1_17_1.BLOCK_ENTITY_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14);
                handler(wrapper -> {
                    final short id = wrapper.read(Type.UNSIGNED_BYTE);
                    wrapper.write(Type.VAR_INT, BlockEntityIds.newId(id));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.UPDATE_LIGHT, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int chunkX = wrapper.passthrough(Type.VAR_INT);
                    final int chunkZ = wrapper.passthrough(Type.VAR_INT);
                    if (wrapper.user().get(ChunkLightStorage.class).isLoaded(chunkX, chunkZ)) {
                        // Light packets updating already sent chunks are the same as before
                        return;
                    }

                    wrapper.cancel();

                    final boolean trustEdges = wrapper.read(Type.BOOLEAN);
                    final long[] skyLightMask = wrapper.read(Type.LONG_ARRAY_PRIMITIVE);
                    final long[] blockLightMask = wrapper.read(Type.LONG_ARRAY_PRIMITIVE);
                    final long[] emptySkyLightMask = wrapper.read(Type.LONG_ARRAY_PRIMITIVE);
                    final long[] emptyBlockLightMask = wrapper.read(Type.LONG_ARRAY_PRIMITIVE);

                    final int skyLightLenght = wrapper.read(Type.VAR_INT);
                    final byte[][] skyLight = new byte[skyLightLenght][];
                    for (int i = 0; i < skyLightLenght; i++) {
                        skyLight[i] = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                    }

                    final int blockLightLength = wrapper.read(Type.VAR_INT);
                    final byte[][] blockLight = new byte[blockLightLength][];
                    for (int i = 0; i < blockLightLength; i++) {
                        blockLight[i] = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                    }

                    final ChunkLightStorage lightStorage = wrapper.user().get(ChunkLightStorage.class);
                    lightStorage.storeLight(chunkX, chunkZ,
                            new ChunkLightStorage.ChunkLight(trustEdges, skyLightMask, blockLightMask,
                                    emptySkyLightMask, emptyBlockLightMask, skyLight, blockLight));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
                    final Chunk oldChunk = wrapper.read(new Chunk1_17Type(tracker.currentWorldSectionHeight()));

                    final List<BlockEntity> blockEntities = new ArrayList<>(oldChunk.getBlockEntities().size());
                    for (final CompoundTag tag : oldChunk.getBlockEntities()) {
                        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z") || !tag.contains("id")) {
                            continue;
                        }

                        final int x = ((NumberTag) tag.get("x")).asInt();
                        final int z = ((NumberTag) tag.get("z")).asInt();
                        final byte packedXZ = (byte) ((x & 15) << 4 | (z & 15));

                        final short y = ((NumberTag) tag.get("y")).asShort();
                        final String id = ((StringTag) tag.get("id")).getValue();
                        final int typeId = protocol.getMappingData().blockEntityIds().getInt(id.replace("minecraft:", ""));
                        if (typeId == -1) {
                            Via.getPlatform().getLogger().warning("Unknown block entity: " + id);
                        }

                        blockEntities.add(new BlockEntityImpl(packedXZ, y, typeId, tag));
                    }

                    final int[] biomeData = oldChunk.getBiomeData();
                    final ChunkSection[] sections = oldChunk.getSections();
                    for (int i = 0; i < sections.length; i++) {
                        ChunkSection section = sections[i];
                        if (section == null) {
                            // There's no section mask anymore
                            section = new ChunkSectionImpl();
                            sections[i] = section;
                            section.setNonAirBlocksCount(0);

                            final DataPaletteImpl blockPalette = new DataPaletteImpl();
                            blockPalette.addId(0);
                            section.addPalette(PaletteType.BLOCKS, blockPalette);
                        } else {
                            /*final DataPalette blockpalette = section.palette(PaletteType.BLOCKS);
                            for (int j = 0; j < blockpalette.size(); j++) {
                                final int old = blockpalette.entry(j);
                                blockpalette.setEntry(j, protocol.getMappingData().getNewBlockStateId(old));
                            }*/
                        }

                        // Fill biome palette
                        final DataPaletteImpl biomePalette = new DataPaletteImpl();
                        section.addPalette(PaletteType.BIOMES, biomePalette);
                        for (int biomeIndex = i * BIOMES_PER_CHUNK; biomeIndex < (i * BIOMES_PER_CHUNK) + BIOMES_PER_CHUNK; biomeIndex++) {
                            final int biome = biomeData[biomeIndex];
                            final int minX = (biomeIndex & HORIZONTAL_MASK) << 2;
                            final int minY = ((biomeIndex >> WIDTH_BITS + WIDTH_BITS) << 2) & 15;
                            final int minZ = (biomeIndex >> WIDTH_BITS & HORIZONTAL_MASK) << 2;
                            for (int x = minX; x < minX + 4; x++) {
                                for (int y = minY; y < minY + 4; y++) {
                                    for (int z = minZ; z < minZ + 4; z++) {
                                        biomePalette.setIdAt(x, y, z, biome);
                                    }
                                }
                            }
                        }
                    }

                    final Chunk chunk = new Chunk1_18(oldChunk.getX(), oldChunk.getZ(), oldChunk.getSections(), oldChunk.getHeightMap(), blockEntities);
                    wrapper.write(new Chunk1_18Type(tracker.currentWorldSectionHeight(),
                            MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().size()),
                            MathUtil.ceilLog2(tracker.biomesSent())), chunk);

                    // Get and remove light stored, there's only full chunk packets //TODO Only get, not remove if we find out people re-send full chunk packets without re-sending light
                    final ChunkLightStorage lightStorage = wrapper.user().get(ChunkLightStorage.class);
                    final ChunkLightStorage.ChunkLight light = lightStorage.removeLight(chunk.getX(), chunk.getZ());
                    if (light == null) {
                        Via.getPlatform().getLogger().warning("No light data found for chunk at " + chunk.getX() + ", " + chunk.getZ());
                        wrapper.cancel();
                        return;
                    }

                    lightStorage.addLoadedChunk(chunk.getX(), chunk.getZ());

                    // Append light data to chunk packet
                    wrapper.write(Type.BOOLEAN, light.trustEdges());
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, light.skyLightMask());
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, light.blockLightMask());
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, light.emptySkyLightMask());
                    wrapper.write(Type.LONG_ARRAY_PRIMITIVE, light.emptyBlockLightMask());
                    wrapper.write(Type.VAR_INT, light.skyLight().length);
                    for (final byte[] skyLight : light.skyLight()) {
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, skyLight);
                    }
                    wrapper.write(Type.VAR_INT, light.blockLight().length);
                    for (final byte[] blockLight : light.blockLight()) {
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, blockLight);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.UNLOAD_CHUNK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int chunkX = wrapper.passthrough(Type.INT);
                    final int chunkZ = wrapper.passthrough(Type.INT);
                    wrapper.user().get(ChunkLightStorage.class).clear(chunkX, chunkZ);
                });
            }
        });
    }
}
