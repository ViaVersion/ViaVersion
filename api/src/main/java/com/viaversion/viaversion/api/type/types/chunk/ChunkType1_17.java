/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public final class ChunkType1_17 extends Type<Chunk> {
    private static final CompoundTag[] EMPTY_COMPOUNDS = new CompoundTag[0];
    private final int ySectionCount;

    public ChunkType1_17(int ySectionCount) {
        super(Chunk.class);
        Preconditions.checkArgument(ySectionCount > 0);
        this.ySectionCount = ySectionCount;
    }

    @Override
    public Chunk read(ByteBuf input) {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        BitSet sectionsMask = BitSet.valueOf(Types.LONG_ARRAY_PRIMITIVE.read(input));
        CompoundTag heightMap = Types.NAMED_COMPOUND_TAG.read(input);

        int[] biomeData = Types.VAR_INT_ARRAY_PRIMITIVE.read(input);

        ByteBuf data = input.readSlice(Types.VAR_INT.readPrimitive(input));

        // Read sections
        ChunkSection[] sections = new ChunkSection[ySectionCount];
        for (int i = 0; i < ySectionCount; i++) {
            if (!sectionsMask.get(i)) continue; // Section not set

            short nonAirBlocksCount = data.readShort();
            ChunkSection section = Types.CHUNK_SECTION1_16.read(data);
            section.setNonAirBlocksCount(nonAirBlocksCount);
            sections[i] = section;
        }

        List<CompoundTag> nbtData = new ArrayList<>(Arrays.asList(Types.NAMED_COMPOUND_TAG_ARRAY.read(input)));
        return new BaseChunk(chunkX, chunkZ, true, false, sectionsMask, sections, biomeData, heightMap, nbtData);
    }

    @Override
    public void write(ByteBuf output, Chunk chunk) {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        Types.LONG_ARRAY_PRIMITIVE.write(output, chunk.getChunkMask().toLongArray());
        Types.NAMED_COMPOUND_TAG.write(output, chunk.getHeightMap());

        // Write biome data
        Types.VAR_INT_ARRAY_PRIMITIVE.write(output, chunk.getBiomeData());

        ByteBuf buf = output.alloc().buffer();
        try {
            ChunkSection[] sections = chunk.getSections();
            for (ChunkSection section : sections) {
                if (section == null) continue; // Section not set

                buf.writeShort(section.getNonAirBlocksCount());
                Types.CHUNK_SECTION1_16.write(buf, section);
            }
            buf.readerIndex(0);
            Types.VAR_INT.writePrimitive(output, buf.readableBytes());
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        // Write Block Entities
        Types.NAMED_COMPOUND_TAG_ARRAY.write(output, chunk.getBlockEntities().toArray(EMPTY_COMPOUNDS));
    }
}
