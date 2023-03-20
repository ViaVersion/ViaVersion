/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.MathUtil;
import java.util.List;

public class BlockRewriter<C extends ClientboundPacketType> {
    private final Protocol<C, ?, ?, ?> protocol;
    private final Type<Position> positionType;

    public BlockRewriter(Protocol<C, ?, ?, ?> protocol, Type<Position> positionType) {
        this.protocol = protocol;
        this.positionType = positionType;
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
                    if (id == playRecordId) { // Play record
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewItemId(data));
                    } else if (id == blockBreakId) { // Block break + block break sound
                        wrapper.set(Type.INT, 1, protocol.getMappingData().getNewBlockStateId(data));
                    }
                });
            }
        });
    }

    public void registerChunkData1_19(C packetType, ChunkTypeSupplier chunkTypeSupplier) {
        protocol.registerClientbound(packetType, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            Preconditions.checkArgument(tracker.biomesSent() != 0, "Biome count not set");
            Preconditions.checkArgument(tracker.currentWorldSectionHeight() != 0, "Section height not set");
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
            if (blockEntityMappings != null) {
                List<BlockEntity> blockEntities = chunk.blockEntities();
                for (int i = 0; i < blockEntities.size(); i++) {
                    final BlockEntity blockEntity = blockEntities.get(i);
                    blockEntities.set(i, blockEntity.withTypeId(protocol.getMappingData().getBlockEntityMappings().getNewIdOrDefault(blockEntity.typeId(), blockEntity.typeId())));
                }
            }
        });
    }

    public void registerBlockEntityData(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_14);
                handler(wrapper -> {
                    final Mappings mappings = protocol.getMappingData().getBlockEntityMappings();
                    if (mappings != null) {
                        final int blockEntityId = wrapper.read(Type.VAR_INT);
                        wrapper.write(Type.VAR_INT, mappings.getNewIdOrDefault(blockEntityId, blockEntityId));
                    }
                });
            }
        });
    }

    @FunctionalInterface
    public interface ChunkTypeSupplier {

        Type<Chunk> supply(int ySectionCount, int globalPaletteBlockBits, int globalPaletteBiomeBits);
    }
}
