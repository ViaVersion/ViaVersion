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
package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.MathUtil;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockRewriter<C extends ClientboundPacketType> {
    private final Protocol<C, ?, ?, ?> protocol;
    private final Type<Position> positionType;
    private final Type<CompoundTag> compoundTagType;

    public BlockRewriter(Protocol<C, ?, ?, ?> protocol, Type<Position> positionType, Type<CompoundTag> compoundTagType) {
        this.protocol = protocol;
        this.positionType = positionType;
        this.compoundTagType = compoundTagType;
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> legacy(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_8, Types.NAMED_COMPOUND_TAG);
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> for1_14(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_14, Types.NAMED_COMPOUND_TAG);
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> for1_20_2(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_14, Types.COMPOUND_TAG);
    }

    public void registerBlockEvent(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(positionType); // Location
                map(Types.UNSIGNED_BYTE); // Action id
                map(Types.UNSIGNED_BYTE); // Action param
                map(Types.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(wrapper -> {
                    if (protocol.getMappingData().getBlockMappings() == null) {
                        return;
                    }

                    int id = wrapper.get(Types.VAR_INT, 0);
                    int mappedId = protocol.getMappingData().getNewBlockId(id);
                    if (mappedId == -1) {
                        // Block (action) has been removed
                        wrapper.cancel();
                        return;
                    }

                    wrapper.set(Types.VAR_INT, 0, mappedId);
                });
            }
        });
    }

    public void registerBlockUpdate(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(positionType);
                map(Types.VAR_INT);
                handler(wrapper -> wrapper.set(Types.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.VAR_INT, 0))));
            }
        });
    }

    public void registerChunkBlocksUpdate(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Chunk X
                map(Types.INT); // 1 - Chunk Z
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Types.BLOCK_CHANGE_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerSectionBlocksUpdate(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.LONG); // Chunk position
                map(Types.BOOLEAN); // Suppress light updates
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerSectionBlocksUpdate1_20(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.LONG); // Chunk position
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerBlockBreakAck(C packetType) {
        // Same exact handler
        registerBlockUpdate(packetType);
    }

    public void registerLevelEvent(C packetType, int playRecordId, int blockBreakId) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Effect Id
                map(positionType); // Location
                map(Types.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    int data = wrapper.get(Types.INT, 1);
                    if (id == playRecordId && protocol.getMappingData().getItemMappings() != null) {
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(data));
                    } else if (id == blockBreakId && protocol.getMappingData().getBlockStateMappings() != null) {
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                    }
                });
            }
        });
    }

    public void registerLevelChunk(C packetType, Type<Chunk> chunkType, Type<Chunk> newChunkType) {
        registerLevelChunk(packetType, chunkType, newChunkType, null);
    }

    public void registerLevelChunk(C packetType, Type<Chunk> chunkType, Type<Chunk> newChunkType, @Nullable BiConsumer<UserConnection, Chunk> chunkRewriter) {
        protocol.registerClientbound(packetType, wrapper -> {
            Chunk chunk = wrapper.read(chunkType);
            wrapper.write(newChunkType, chunk);

            handleChunk(chunk);
            if (chunkRewriter != null) {
                chunkRewriter.accept(wrapper.user(), chunk);
            }
        });
    }

    public void handleChunk(Chunk chunk) {
        for (int s = 0; s < chunk.getSections().length; s++) {
            ChunkSection section = chunk.getSections()[s];
            if (section == null) {
                continue;
            }

            DataPalette palette = section.palette(PaletteType.BLOCKS);
            for (int i = 0; i < palette.size(); i++) {
                int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(palette.idByIndex(i));
                palette.setIdByIndex(i, mappedBlockStateId);
            }
        }
    }

    public void registerLevelChunk1_19(C packetType, ChunkTypeSupplier chunkTypeSupplier) {
        registerLevelChunk1_19(packetType, chunkTypeSupplier, null);
    }

    public void registerLevelChunk1_19(C packetType, ChunkTypeSupplier chunkTypeSupplier, @Nullable BiConsumer<UserConnection, BlockEntity> blockEntityHandler) {
        protocol.registerClientbound(packetType, chunkHandler1_19(chunkTypeSupplier, blockEntityHandler));
    }

    public PacketHandler chunkHandler1_19(ChunkTypeSupplier chunkTypeSupplier, @Nullable BiConsumer<UserConnection, BlockEntity> blockEntityHandler) {
        return wrapper -> {
            final Chunk chunk = handleChunk1_19(wrapper, chunkTypeSupplier);
            final Mappings blockEntityMappings = protocol.getMappingData().getBlockEntityMappings();
            if (blockEntityMappings != null || blockEntityHandler != null) {
                final List<BlockEntity> blockEntities = chunk.blockEntities();
                for (int i = 0; i < blockEntities.size(); i++) {
                    final BlockEntity blockEntity = blockEntities.get(i);
                    if (blockEntityMappings != null) {
                        final int id = blockEntity.typeId();
                        final int mappedId = blockEntityMappings.getNewIdOrDefault(id, id);
                        if (id != mappedId) {
                            blockEntities.set(i, blockEntity.withTypeId(mappedId));
                        }
                    }

                    if (blockEntityHandler != null && blockEntity.tag() != null) {
                        blockEntityHandler.accept(wrapper.user(), blockEntity);
                    }
                }
            }
        };
    }

    public Chunk handleChunk1_19(PacketWrapper wrapper, ChunkTypeSupplier chunkTypeSupplier) {
        final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
        Preconditions.checkArgument(tracker.biomesSent() != -1, "Biome count not set");
        Preconditions.checkArgument(tracker.currentWorldSectionHeight() != -1, "Section height not set");
        final Type<Chunk> chunkType = chunkTypeSupplier.supply(tracker.currentWorldSectionHeight(),
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
        return chunk;
    }

    public void registerBlockEntityData(C packetType) {
        registerBlockEntityData(packetType, null);
    }

    public void registerBlockEntityData(C packetType, @Nullable Consumer<BlockEntity> blockEntityHandler) {
        protocol.registerClientbound(packetType, wrapper -> {
            final Position position = wrapper.passthrough(positionType);

            final int blockEntityId = wrapper.read(Types.VAR_INT);
            final Mappings mappings = protocol.getMappingData().getBlockEntityMappings();
            if (mappings != null) {
                wrapper.write(Types.VAR_INT, mappings.getNewIdOrDefault(blockEntityId, blockEntityId));
            } else {
                wrapper.write(Types.VAR_INT, blockEntityId);
            }

            final CompoundTag tag;
            if (blockEntityHandler != null && (tag = wrapper.passthrough(compoundTagType)) != null) {
                final BlockEntity blockEntity = new BlockEntityImpl(BlockEntity.pack(position.x(), position.z()), (short) position.y(), blockEntityId, tag);
                blockEntityHandler.accept(blockEntity);
            }
        });
    }

    @FunctionalInterface
    public interface ChunkTypeSupplier {

        Type<Chunk> supply(int ySectionCount, int globalPaletteBlockBits, int globalPaletteBiomeBits);
    }
}
