/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.type.types.chunk;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class ChunkType1_20_2 extends Type<Chunk> {
    private final ChunkSectionType1_18 sectionType;
    private final int ySectionCount;

    public ChunkType1_20_2(final int ySectionCount, final int globalPaletteBlockBits, final int globalPaletteBiomeBits) {
        super(Chunk.class);
        Preconditions.checkArgument(ySectionCount > 0);
        this.sectionType = new ChunkSectionType1_18(globalPaletteBlockBits, globalPaletteBiomeBits);
        this.ySectionCount = ySectionCount;
    }

    @Override
    public Chunk read(final ByteBuf buffer) throws Exception {
        final int chunkX = buffer.readInt();
        final int chunkZ = buffer.readInt();
        final CompoundTag heightMap = Type.COMPOUND_TAG.read(buffer);

        // Read sections
        final ByteBuf sectionsBuf = buffer.readBytes(Type.VAR_INT.readPrimitive(buffer));
        final ChunkSection[] sections = new ChunkSection[ySectionCount];
        try {
            for (int i = 0; i < ySectionCount; i++) {
                sections[i] = sectionType.read(sectionsBuf);
            }
        } finally {
            sectionsBuf.release();
        }

        final int blockEntitiesLength = Type.VAR_INT.readPrimitive(buffer);
        final List<BlockEntity> blockEntities = new ArrayList<>(blockEntitiesLength);
        for (int i = 0; i < blockEntitiesLength; i++) {
            blockEntities.add(Type.BLOCK_ENTITY1_20_2.read(buffer));
        }

        return new Chunk1_18(chunkX, chunkZ, sections, heightMap, blockEntities);
    }

    @Override
    public void write(final ByteBuf buffer, final Chunk chunk) throws Exception {
        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());

        Type.COMPOUND_TAG.write(buffer, chunk.getHeightMap());

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
            Type.BLOCK_ENTITY1_20_2.write(buffer, blockEntity);
        }
    }
}
