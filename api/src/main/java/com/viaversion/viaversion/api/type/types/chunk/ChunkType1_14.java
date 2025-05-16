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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkType1_14 extends Type<Chunk> {

    public static final Type<Chunk> TYPE = new ChunkType1_14();

    public ChunkType1_14() {
        super(Chunk.class);
    }

    @Override
    public Chunk read(ByteBuf input) {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean fullChunk = input.readBoolean();
        int primaryBitmask = Types.VAR_INT.readPrimitive(input);
        CompoundTag heightMap = Types.NAMED_COMPOUND_TAG.read(input);
        ByteBuf data = input.readSlice(Types.VAR_INT.readPrimitive(input));

        // Read sections
        ChunkSection[] sections = new ChunkSection[16];
        for (int i = 0; i < 16; i++) {
            if ((primaryBitmask & (1 << i)) == 0) continue; // Section not set

            short nonAirBlocksCount = data.readShort();
            ChunkSection section = Types.CHUNK_SECTION1_13.read(data);
            section.setNonAirBlocksCount(nonAirBlocksCount);
            sections[i] = section;
        }

        int[] biomeData = fullChunk ? new int[256] : null;
        if (fullChunk) {
            for (int i = 0; i < 256; i++) {
                biomeData[i] = data.readInt();
            }
        }

        List<CompoundTag> nbtData = new ArrayList<>(Arrays.asList(Types.NAMED_COMPOUND_TAG_ARRAY.read(input)));
        return new BaseChunk(chunkX, chunkZ, fullChunk, false, primaryBitmask, sections, biomeData, heightMap, nbtData);
    }

    @Override
    public void write(ByteBuf output, Chunk chunk) {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        output.writeBoolean(chunk.isFullChunk());
        Types.VAR_INT.writePrimitive(output, chunk.getBitmask());
        Types.NAMED_COMPOUND_TAG.write(output, chunk.getHeightMap());

        ByteBuf buf = output.alloc().buffer();
        try {
            for (int i = 0; i < 16; i++) {
                ChunkSection section = chunk.getSections()[i];
                if (section == null) continue; // Section not set

                buf.writeShort(section.getNonAirBlocksCount());
                Types.CHUNK_SECTION1_13.write(buf, section);
            }
            buf.readerIndex(0);
            Types.VAR_INT.writePrimitive(output, buf.readableBytes() + (chunk.isBiomeData() ? 1024 : 0)); // 256 * 4
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        // Write biome data
        if (chunk.isBiomeData()) {
            for (int value : chunk.getBiomeData()) {
                output.writeInt(value & 0xFF); // This is a temporary workaround, we'll look into fixing this soon :)
            }
        }

        // Write Block Entities
        Types.NAMED_COMPOUND_TAG_ARRAY.write(output, chunk.getBlockEntities().toArray(new CompoundTag[0]));
    }
}
