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
package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.MathUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockRewriter<C extends ClientboundPacketType> {
    private final Protocol<C, ?, ?, ?> protocol;
    private final Type<BlockPosition> positionType;
    private final Type<CompoundTag> compoundTagType;

    public BlockRewriter(Protocol<C, ?, ?, ?> protocol, Type<BlockPosition> positionType, Type<CompoundTag> compoundTagType) {
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
        protocol.registerClientbound(packetType, wrapper -> {
            if (protocol.getMappingData().getBlockMappings() == null) {
                return;
            }

            wrapper.passthrough(positionType); // Location
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Action id
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Action param
            final int blockId = wrapper.passthrough(Types.VAR_INT);
            final int mappedId = protocol.getMappingData().getNewBlockId(blockId);
            if (mappedId == -1) {
                wrapper.cancel();
                return;
            }

            if (blockId != mappedId) {
                wrapper.set(Types.VAR_INT, 0, mappedId);
            }
        });
    }

    public void registerBlockUpdate(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(positionType);

            final int blockId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockId));
        });
    }

    public void registerChunkBlocksUpdate(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.INT); // Chunk X
            wrapper.passthrough(Types.INT); // Chunk Z
            for (BlockChangeRecord record : wrapper.passthrough(Types.BLOCK_CHANGE_ARRAY)) {
                record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
        });
    }

    public void registerSectionBlocksUpdate(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.LONG); // Chunk position
            wrapper.passthrough(Types.BOOLEAN); // Suppress light updates
            for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
        });
    }

    public void registerSectionBlocksUpdate1_20(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.LONG); // Chunk position
            for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
        });
    }

    public void registerBlockBreakAck(C packetType) {
        // Same exact handler
        registerBlockUpdate(packetType);
    }

    public void registerLevelEvent(C packetType, int playRecordId, int blockBreakId) {
        protocol.registerClientbound(packetType, wrapper -> {
            final int id = wrapper.passthrough(Types.INT);
            wrapper.passthrough(positionType);

            final int data = wrapper.read(Types.INT);
            final MappingData mappingData = protocol.getMappingData();
            if (playRecordId != -1 && id == playRecordId && mappingData.getItemMappings() != null) {
                wrapper.write(Types.INT, mappingData.getNewItemId(data));
            } else if (id == blockBreakId && mappingData.getBlockStateMappings() != null) {
                wrapper.write(Types.INT, mappingData.getNewBlockStateId(data));
            } else {
                wrapper.write(Types.INT, data);
            }
        });
    }

    public void registerLevelEvent1_21(C packetType, int blockBreakId) {
        registerLevelEvent(packetType, -1, blockBreakId);
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
            handleBlockEntities(blockEntityHandler, chunk, wrapper.user());
        };
    }

    public void handleBlockEntities(BiConsumer<UserConnection, BlockEntity> blockEntityHandler, Chunk chunk, UserConnection connection) {
        final Mappings blockEntityMappings = protocol.getMappingData().getBlockEntityMappings();
        if (blockEntityMappings == null && blockEntityHandler == null) {
            return;
        }

        final List<BlockEntity> blockEntities = chunk.blockEntities();
        final IntList toRemove = new IntArrayList(0);
        for (int i = 0; i < blockEntities.size(); i++) {
            final BlockEntity blockEntity = blockEntities.get(i);
            if (blockEntityMappings != null) {
                final int id = blockEntity.typeId();
                final int mappedId = blockEntityMappings.getNewId(id);
                if (mappedId == -1) {
                    toRemove.add(i);
                    continue;
                }

                if (id != mappedId) {
                    blockEntities.set(i, blockEntity.withTypeId(mappedId));
                }
            }

            if (blockEntityHandler != null && blockEntity.tag() != null) {
                blockEntityHandler.accept(connection, blockEntity);
            }
        }

        if (!toRemove.isEmpty()) {
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                blockEntities.remove(toRemove.getInt(i));
            }
        }
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
        registerBlockEntityData(packetType, (BiConsumer<UserConnection, BlockEntity>) null);
    }

    public void registerBlockEntityData(C packetType, @Nullable Consumer<BlockEntity> blockEntityHandler) {
        registerBlockEntityData(packetType, blockEntityHandler != null ? (connection, blockEntity) -> blockEntityHandler.accept(blockEntity) : null);
    }

    public void registerBlockEntityData(C packetType, @Nullable BiConsumer<UserConnection, BlockEntity> blockEntityHandler) {
        protocol.registerClientbound(packetType, wrapper -> {
            final BlockPosition position = wrapper.passthrough(positionType);

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
                blockEntityHandler.accept(wrapper.user(), blockEntity);
            }
        });
    }

    @FunctionalInterface
    public interface ChunkTypeSupplier {

        Type<Chunk> supply(int ySectionCount, int globalPaletteBlockBits, int globalPaletteBiomeBits);
    }
}
