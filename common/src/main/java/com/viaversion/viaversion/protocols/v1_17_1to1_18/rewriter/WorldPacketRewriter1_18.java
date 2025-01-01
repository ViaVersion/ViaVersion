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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_17;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.data.BlockEntities1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.data.BlockEntityMappings1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.storage.ChunkLightStorage;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.MathUtil;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class WorldPacketRewriter1_18 {

    public static void register(final Protocol1_17_1To1_18 protocol) {
        protocol.registerClientbound(ClientboundPackets1_17_1.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_14);
                handler(wrapper -> {
                    final short id = wrapper.read(Types.UNSIGNED_BYTE);
                    final int newId = BlockEntityMappings1_18.newId(id);
                    wrapper.write(Types.VAR_INT, newId);

                    handleSpawners(newId, wrapper.passthrough(Types.NAMED_COMPOUND_TAG));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.LIGHT_UPDATE, wrapper -> {
            final int chunkX = wrapper.passthrough(Types.VAR_INT);
            final int chunkZ = wrapper.passthrough(Types.VAR_INT);

            if (wrapper.user().get(ChunkLightStorage.class).isLoaded(chunkX, chunkZ)) {
                if (!Via.getConfig().cache1_17Light()) {
                    // Light packets updating already sent chunks are the same as before
                    return;
                }
                // Pass through and cache light data
            } else {
                // Cancel and cache the light data
                wrapper.cancel();
            }

            final boolean trustEdges = wrapper.passthrough(Types.BOOLEAN);
            final long[] skyLightMask = wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
            final long[] blockLightMask = wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
            final long[] emptySkyLightMask = wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
            final long[] emptyBlockLightMask = wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);

            final int skyLightLenght = wrapper.passthrough(Types.VAR_INT);
            final byte[][] skyLight = new byte[skyLightLenght][];
            for (int i = 0; i < skyLightLenght; i++) {
                skyLight[i] = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
            }

            final int blockLightLength = wrapper.passthrough(Types.VAR_INT);
            final byte[][] blockLight = new byte[blockLightLength][];
            for (int i = 0; i < blockLightLength; i++) {
                blockLight[i] = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
            }

            final ChunkLightStorage lightStorage = wrapper.user().get(ChunkLightStorage.class);
            lightStorage.storeLight(chunkX, chunkZ,
                new ChunkLightStorage.ChunkLight(trustEdges, skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask, skyLight, blockLight));
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.LEVEL_CHUNK, ClientboundPackets1_18.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            final Chunk oldChunk = wrapper.read(new ChunkType1_17(tracker.currentWorldSectionHeight()));

            final List<BlockEntity> blockEntities = new ArrayList<>(oldChunk.getBlockEntities().size());
            for (final CompoundTag tag : oldChunk.getBlockEntities()) {
                final NumberTag xTag = tag.getNumberTag("x");
                final NumberTag yTag = tag.getNumberTag("y");
                final NumberTag zTag = tag.getNumberTag("z");
                final StringTag idTag = tag.getStringTag("id");
                if (xTag == null || yTag == null || zTag == null || idTag == null) {
                    continue;
                }

                final String id = idTag.getValue();
                final int typeId = BlockEntities1_18.blockEntityIds().getInt(Key.stripMinecraftNamespace(id));
                if (typeId == -1) {
                    protocol.getLogger().warning("Unknown block entity: " + id);
                }

                handleSpawners(typeId, tag);

                final byte packedXZ = (byte) ((xTag.asInt() & 15) << 4 | (zTag.asInt() & 15));
                blockEntities.add(new BlockEntityImpl(packedXZ, yTag.asShort(), typeId, tag));
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

                    final DataPaletteImpl blockPalette = new DataPaletteImpl(ChunkSection.SIZE);
                    blockPalette.addId(0);
                    section.addPalette(PaletteType.BLOCKS, blockPalette);
                }

                // Fill biome palette
                final DataPaletteImpl biomePalette = new DataPaletteImpl(ChunkSection.BIOME_SIZE);
                section.addPalette(PaletteType.BIOMES, biomePalette);

                final int offset = i * ChunkSection.BIOME_SIZE;
                for (int biomeIndex = 0, biomeArrayIndex = offset; biomeIndex < ChunkSection.BIOME_SIZE; biomeIndex++, biomeArrayIndex++) {
                    // Also catch invalid biomes with id -1
                    final int biome = biomeData[biomeArrayIndex];
                    biomePalette.setIdAt(biomeIndex, biome != -1 ? biome : 0);
                }
            }

            final Chunk chunk = new Chunk1_18(oldChunk.getX(), oldChunk.getZ(), sections, oldChunk.getHeightMap(), blockEntities);
            wrapper.write(new ChunkType1_18(tracker.currentWorldSectionHeight(),
                MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().mappedSize()),
                MathUtil.ceilLog2(tracker.biomesSent())), chunk);

            final ChunkLightStorage lightStorage = wrapper.user().get(ChunkLightStorage.class);
            final boolean alreadyLoaded = !lightStorage.addLoadedChunk(chunk.getX(), chunk.getZ());

            // Append light data to chunk packet
            final ChunkLightStorage.ChunkLight light = Via.getConfig().cache1_17Light() ?
                lightStorage.getLight(chunk.getX(), chunk.getZ()) : lightStorage.removeLight(chunk.getX(), chunk.getZ());
            if (light == null) {
                protocol.getLogger().warning("No light data found for chunk at " + chunk.getX() + ", " + chunk.getZ() + ". Chunk was already loaded: " + alreadyLoaded);
                protocol.getLogger().warning("This means another plugin is sending chunk data in a bad order, or you need to re-enable the cache-1_17-light config option.");

                final BitSet emptyLightMask = new BitSet();
                emptyLightMask.set(0, tracker.currentWorldSectionHeight() + 2);
                wrapper.write(Types.BOOLEAN, false);
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, new long[0]);
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, new long[0]);
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, emptyLightMask.toLongArray());
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, emptyLightMask.toLongArray());
                wrapper.write(Types.VAR_INT, 0);
                wrapper.write(Types.VAR_INT, 0);
            } else {
                wrapper.write(Types.BOOLEAN, light.trustEdges());
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, light.skyLightMask());
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, light.blockLightMask());
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, light.emptySkyLightMask());
                wrapper.write(Types.LONG_ARRAY_PRIMITIVE, light.emptyBlockLightMask());
                wrapper.write(Types.VAR_INT, light.skyLight().length);
                for (final byte[] skyLight : light.skyLight()) {
                    wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, skyLight);
                }
                wrapper.write(Types.VAR_INT, light.blockLight().length);
                for (final byte[] blockLight : light.blockLight()) {
                    wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, blockLight);
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_17_1.FORGET_LEVEL_CHUNK, wrapper -> {
            final int chunkX = wrapper.passthrough(Types.INT);
            final int chunkZ = wrapper.passthrough(Types.INT);
            wrapper.user().get(ChunkLightStorage.class).clear(chunkX, chunkZ);
        });
    }

    private static void handleSpawners(int typeId, final CompoundTag tag) {
        if (typeId == 8) {
            final CompoundTag entity = tag.getCompoundTag("SpawnData");
            if (entity != null) {
                final CompoundTag spawnData = new CompoundTag();
                tag.put("SpawnData", spawnData);
                spawnData.put("entity", entity);
            }
        }
    }
}
