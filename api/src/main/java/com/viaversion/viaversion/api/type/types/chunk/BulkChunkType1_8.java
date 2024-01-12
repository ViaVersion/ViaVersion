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

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class BulkChunkType1_8 extends Type<Chunk[]> {

    public static final Type<Chunk[]> TYPE = new BulkChunkType1_8();
    private static final int BLOCKS_PER_SECTION = 16 * 16 * 16;
    private static final int BLOCKS_BYTES = BLOCKS_PER_SECTION * 2;
    private static final int LIGHT_BYTES = BLOCKS_PER_SECTION / 2;
    private static final int BIOME_BYTES = 16 * 16;

    public BulkChunkType1_8() {
        super(Chunk[].class);
    }

    @Override
    public Chunk[] read(ByteBuf input) throws Exception {
        final boolean skyLight = input.readBoolean();
        final int count = Type.VAR_INT.readPrimitive(input);
        final Chunk[] chunks = new Chunk[count];
        final ChunkBulkSection[] chunkInfo = new ChunkBulkSection[count];

        // Read metadata
        for (int i = 0; i < chunkInfo.length; i++) {
            chunkInfo[i] = new ChunkBulkSection(input, skyLight);
        }
        // Read data
        for (int i = 0; i < chunks.length; i++) {
            final ChunkBulkSection chunkBulkSection = chunkInfo[i];
            chunkBulkSection.readData(input);
            chunks[i] = ChunkType1_8.deserialize(chunkBulkSection.chunkX, chunkBulkSection.chunkZ, true, skyLight, chunkBulkSection.bitmask, chunkBulkSection.data());
        }

        return chunks;
    }

    @Override
    public void write(ByteBuf output, Chunk[] chunks) throws Exception {
        boolean skyLight = false;
        loop1:
        for (Chunk chunk : chunks) {
            for (ChunkSection section : chunk.getSections()) {
                if (section != null && section.getLight().hasSkyLight()) {
                    skyLight = true;
                    break loop1;
                }
            }
        }
        output.writeBoolean(skyLight);
        Type.VAR_INT.writePrimitive(output, chunks.length);

        // Write metadata
        for (Chunk chunk : chunks) {
            output.writeInt(chunk.getX());
            output.writeInt(chunk.getZ());
            output.writeShort(chunk.getBitmask());
        }
        // Write data
        for (Chunk chunk : chunks) {
            output.writeBytes(ChunkType1_8.serialize(chunk));
        }
    }

    public static final class ChunkBulkSection {
        private final int chunkX;
        private final int chunkZ;
        private final int bitmask;
        private final byte[] data;

        public ChunkBulkSection(final ByteBuf input, final boolean skyLight) {
            this.chunkX = input.readInt();
            this.chunkZ = input.readInt();
            this.bitmask = input.readUnsignedShort();
            final int setSections = Integer.bitCount(this.bitmask);
            this.data = new byte[setSections * (BLOCKS_BYTES + (skyLight ? 2 * LIGHT_BYTES : LIGHT_BYTES)) + BIOME_BYTES];
        }

        public void readData(final ByteBuf input) {
            input.readBytes(this.data);
        }

        public int chunkX() {
            return this.chunkX;
        }

        public int chunkZ() {
            return this.chunkZ;
        }

        public int bitmask() {
            return this.bitmask;
        }

        public byte[] data() {
            return this.data;
        }
    }
}
