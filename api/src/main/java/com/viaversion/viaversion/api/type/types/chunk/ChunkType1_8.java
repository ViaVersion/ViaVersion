/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.BaseChunkType;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;

public class ChunkType1_8 extends PartialType<Chunk, ClientWorld> {

    public ChunkType1_8(ClientWorld param) {
        super(param, Chunk.class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }

    @Override
    public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
        final int chunkX = input.readInt();
        final int chunkZ = input.readInt();
        final boolean fullChunk = input.readBoolean();
        final int bitmask = input.readUnsignedShort();
        final int dataLength = Type.VAR_INT.readPrimitive(input);
        final byte[] data = new byte[dataLength];
        input.readBytes(data);

        // Check if the chunk is an unload packet and return early
        if (fullChunk && bitmask == 0) {
            return new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], null, new ArrayList<>());
        }

        return deserialize(chunkX, chunkZ, fullChunk, world.getEnvironment() == Environment.NORMAL, bitmask, data);
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        output.writeBoolean(chunk.isFullChunk());
        output.writeShort(chunk.getBitmask());
        final byte[] data = serialize(chunk);
        Type.VAR_INT.writePrimitive(output, data.length);
        output.writeBytes(data);
    }

    // Used for normal and bulk chunks
    public static Chunk deserialize(final int chunkX, final int chunkZ, final boolean fullChunk, final boolean skyLight, final int bitmask, final byte[] data) throws Exception {
        final ByteBuf input = Unpooled.wrappedBuffer(data);

        final ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = null;

        // Read blocks
        for (int i = 0; i < sections.length; i++) {
            if ((bitmask & 1 << i) == 0) continue;
            sections[i] = Types1_8.CHUNK_SECTION.read(input);
        }

        // Read block light
        for (int i = 0; i < sections.length; i++) {
            if ((bitmask & 1 << i) == 0) continue;
            sections[i].getLight().readBlockLight(input);
        }

        // Read sky light
        if (skyLight) {
            for (int i = 0; i < sections.length; i++) {
                if ((bitmask & 1 << i) == 0) continue;
                sections[i].getLight().readSkyLight(input);
            }
        }

        // Read biome data
        if (fullChunk) {
            biomeData = new int[256];
            for (int i = 0; i < 256; i++) {
                biomeData[i] = input.readUnsignedByte();
            }
        }
        input.release();

        return new BaseChunk(chunkX, chunkZ, fullChunk, false, bitmask, sections, biomeData, new ArrayList<>());
    }

    // Used for normal and bulk chunks
    public static byte[] serialize(final Chunk chunk) throws Exception {
        final ByteBuf output = Unpooled.buffer();

        // Write blocks
        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            Types1_8.CHUNK_SECTION.write(output, chunk.getSections()[i]);
        }

        // Write block light
        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            chunk.getSections()[i].getLight().writeBlockLight(output);
        }

        // Write sky light
        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            if (chunk.getSections()[i].getLight().hasSkyLight())
                chunk.getSections()[i].getLight().writeSkyLight(output);
        }

        // Write biome data
        if (chunk.isFullChunk() && chunk.getBiomeData() != null) {
            for (int biome : chunk.getBiomeData()) {
                output.writeByte((byte) biome);
            }
        }
        final byte[] data = new byte[output.readableBytes()];
        output.readBytes(data);
        output.release();

        return data;
    }

}
