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
package com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.BaseChunkType;
import com.viaversion.viaversion.api.type.types.version.ChunkSectionType1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class Chunk1_18Type extends Type<Chunk> {
    private final ChunkSectionType1_18 sectionType;
    private final int ySectionCount;

    public Chunk1_18Type(final int ySectionCount, final int globalPaletteBlockBits, final int globalPaletteBiomeBits) {
        super(Chunk.class);
        Preconditions.checkArgument(ySectionCount > 0);
        this.sectionType = new ChunkSectionType1_18(globalPaletteBlockBits, globalPaletteBiomeBits);
        this.ySectionCount = ySectionCount;
    }

    @Override
    public Chunk read(final ByteBuf buffer) throws Exception {
        final int chunkX = buffer.readInt();
        final int chunkZ = buffer.readInt();
        final CompoundTag heightMap = Type.NBT.read(buffer);

        // Read sections
        final ByteBuf sectionsBuf = buffer.readBytes(Type.VAR_INT.readPrimitive(buffer));
        final ChunkSection[] sections = new ChunkSection[ySectionCount];
        try {
            for (int i = 0; i < ySectionCount; i++) {
                sections[i] = sectionType.read(sectionsBuf);
            }
        } finally {
            /*if (sectionsBuf.readableBytes() > 0 && Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Found " + sectionsBuf.readableBytes() + " more bytes than expected while reading the chunk: " + chunkX + "/" + chunkZ);
            }*/
            sectionsBuf.release();
        }

        final int blockEntitiesLength = Type.VAR_INT.readPrimitive(buffer);
        final List<BlockEntity> blockEntities = new ArrayList<>(blockEntitiesLength);
        for (int i = 0; i < blockEntitiesLength; i++) {
            blockEntities.add(Types1_18.BLOCK_ENTITY.read(buffer));
        }

        return new Chunk1_18(chunkX, chunkZ, sections, heightMap, blockEntities);
    }

    @Override
    public void write(final ByteBuf buffer, final Chunk chunk) throws Exception {
        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());

        Type.NBT.write(buffer, chunk.getHeightMap());

        final ByteBuf sectionBuffer = buffer.alloc().buffer();
        try {
            for (final ChunkSection section : chunk.getSections()) {
                sectionType.write(sectionBuffer, section);
            }
            sectionBuffer.readerIndex(0);
            Type.VAR_INT.writePrimitive(buffer, sectionBuffer.readableBytes());
            buffer.writeBytes(sectionBuffer);
        } finally {
            sectionBuffer.release(); // release buffer
        }

        Type.VAR_INT.writePrimitive(buffer, chunk.blockEntities().size());
        for (final BlockEntity blockEntity : chunk.blockEntities()) {
            Types1_18.BLOCK_ENTITY.write(buffer, blockEntity);
        }
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }
}
