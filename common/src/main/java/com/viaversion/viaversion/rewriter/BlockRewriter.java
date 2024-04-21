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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Preconditions;
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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.MathUtil;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockRewriter<C extends ClientboundPacketType> {
    private final Protocol<C, ?, ?, ?> protocol;
    private final Type<Position> positionType;
    private final Type<CompoundTag> compoundTagType;

    @Deprecated/*(forRemoval = true)*/
    public BlockRewriter(Protocol<C, ?, ?, ?> protocol, Type<Position> positionType) {
        this(protocol, positionType, Type.NAMED_COMPOUND_TAG);
    }

    public BlockRewriter(Protocol<C, ?, ?, ?> protocol, Type<Position> positionType, Type<CompoundTag> compoundTagType) {
        this.protocol = protocol;
        this.positionType = positionType;
        this.compoundTagType = compoundTagType;
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> legacy(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Type.POSITION1_8, Type.NAMED_COMPOUND_TAG);
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> for1_14(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Type.POSITION1_14, Type.NAMED_COMPOUND_TAG);
    }

    public static <C extends ClientboundPacketType> BlockRewriter<C> for1_20_2(final Protocol<C, ?, ?, ?> protocol) {
        return new BlockRewriter<>(protocol, Type.POSITION1_14, Type.COMPOUND_TAG);
    }

    public void registerBlockAction(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(positionType); // Location
                map(Type.UNSIGNED_BYTE); // Action id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block id - /!\ NOT BLOCK STATE
                handler(wrapper -> {
                    if (protocol.getMappingData().getBlockMappings() == null) {
                        return;
                    }

                    int id = wrapper.get(Type.VAR_INT, 0);
                    int mappedId = protocol.getMappingData().getNewBlockId(id);
                    if (mappedId == -1) {
                        // Block (action) has been removed
                        wrapper.cancel();
                        return;
                    }

                    wrapper.set(Type.VAR_INT, 0, mappedId);
                });
            }
        });
    }

    public void registerBlockChange(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(positionType);
                map(Type.VAR_INT);
                handler(wrapper -> wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(wrapper.get(Type.VAR_INT, 0))));
            }
        });
    }

    public void registerMultiBlockChange(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Chunk X
                map(Type.INT); // 1 - Chunk Z
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Type.BLOCK_CHANGE_RECORD_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerVarLongMultiBlockChange(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.LONG); // Chunk position
                map(Type.BOOLEAN); // Suppress light updates
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerVarLongMultiBlockChange1_20(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.LONG); // Chunk position
                handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.passthrough(Type.VAR_LONG_BLOCK_CHANGE_RECORD_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });
    }

    public void registerAcknowledgePlayerDigging(C packetType) {
        // Same exact handler
        registerBlockChange(packetType);
    }

    public void registerEffect(C packetType, int playRecordId, int blockBreakId) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Effect Id
                map(positionType); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    int data = wrapper.get(Type.INT, 1);
                    if (id == playRecordId && protocol.getMappingData().getItemMappings() != null) {
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(data));
                    } else if (id == blockBreakId && protocol.getMappingData().getBlockStateMappings() != null) {
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                    }
                });
            }
        });
    }

    public void registerChunkData1_19(C packetType, ChunkTypeSupplier chunkTypeSupplier) {
        registerChunkData1_19(packetType, chunkTypeSupplier, null);
    }

    public void registerChunkData1_19(C packetType, ChunkTypeSupplier chunkTypeSupplier, @Nullable BiConsumer<UserConnection, BlockEntity> blockEntityHandler) {
        protocol.registerClientbound(packetType, chunkDataHandler1_19(chunkTypeSupplier, blockEntityHandler));
    }

    public PacketHandler chunkDataHandler1_19(ChunkTypeSupplier chunkTypeSupplier, @Nullable BiConsumer<UserConnection, BlockEntity> blockEntityHandler) {
        return wrapper -> {
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

            final Mappings blockEntityMappings = protocol.getMappingData().getBlockEntityMappings();
            if (blockEntityMappings != null || blockEntityHandler != null) {
                List<BlockEntity> blockEntities = chunk.blockEntities();
                for (int i = 0; i < blockEntities.size(); i++) {
                    final BlockEntity blockEntity = blockEntities.get(i);
                    if (blockEntityMappings != null) {
                        blockEntities.set(i, blockEntity.withTypeId(blockEntityMappings.getNewIdOrDefault(blockEntity.typeId(), blockEntity.typeId())));
                    }

                    if (blockEntityHandler != null && blockEntity.tag() != null) {
                        blockEntityHandler.accept(wrapper.user(), blockEntity);
                    }
                }
            }
        };
    }

    public void registerBlockEntityData(C packetType) {
        registerBlockEntityData(packetType, null);
    }

    public void registerBlockEntityData(C packetType, @Nullable Consumer<BlockEntity> blockEntityHandler) {
        protocol.registerClientbound(packetType, wrapper -> {
            final Position position = wrapper.passthrough(positionType);

            final int blockEntityId = wrapper.read(Type.VAR_INT);
            final Mappings mappings = protocol.getMappingData().getBlockEntityMappings();
            if (mappings != null) {
                wrapper.write(Type.VAR_INT, mappings.getNewIdOrDefault(blockEntityId, blockEntityId));
            } else {
                wrapper.write(Type.VAR_INT, blockEntityId);
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
