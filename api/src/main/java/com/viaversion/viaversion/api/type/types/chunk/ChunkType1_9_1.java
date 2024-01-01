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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.util.ChunkUtil;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

public class ChunkType1_9_1 extends Type<Chunk> {

    private static final ChunkType1_9_1 WITH_SKYLIGHT = new ChunkType1_9_1(true);
    private static final ChunkType1_9_1 WITHOUT_SKYLIGHT = new ChunkType1_9_1(false);
    private final boolean hasSkyLight;

    public ChunkType1_9_1(boolean hasSkyLight) {
        super(Chunk.class);
        this.hasSkyLight = hasSkyLight;
    }

    public static ChunkType1_9_1 forEnvironment(Environment environment) {
        return environment == Environment.NORMAL ? WITH_SKYLIGHT : WITHOUT_SKYLIGHT;
    }

    @Override
    public Chunk read(ByteBuf input) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.readPrimitive(input);
        ByteBuf data = input.readSlice(Type.VAR_INT.readPrimitive(input));

        ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = groundUp ? new int[256] : null;
        try {
            BitSet usedSections = new BitSet(16);
            // Calculate section count from bitmask
            for (int i = 0; i < 16; i++) {
                if ((primaryBitmask & (1 << i)) != 0) {
                    usedSections.set(i);
                }
            }

            // Read sections
            for (int i = 0; i < 16; i++) {
                if (!usedSections.get(i)) continue; // Section not set
                ChunkSection section = Types1_9.CHUNK_SECTION.read(data);
                sections[i] = section;
                section.getLight().readBlockLight(data);
                if (hasSkyLight) {
                    section.getLight().readSkyLight(data);
                }
            }

            if (groundUp) {
                for (int i = 0; i < 256; i++) {
                    biomeData[i] = data.readByte() & 0xFF;
                }
            }
        } catch (Throwable e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "The server sent an invalid chunk data packet, returning an empty chunk instead", e);
            return ChunkUtil.createEmptyChunk(chunkX, chunkZ);
        }

        return new BaseChunk(chunkX, chunkZ, groundUp, false, primaryBitmask, sections, biomeData, new ArrayList<>());
    }

    @Override
    public void write(ByteBuf output, Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        output.writeBoolean(chunk.isFullChunk());
        Type.VAR_INT.writePrimitive(output, chunk.getBitmask());

        ByteBuf buf = output.alloc().buffer();
        try {
            for (int i = 0; i < 16; i++) {
                ChunkSection section = chunk.getSections()[i];
                if (section == null) continue; // Section not set
                Types1_9.CHUNK_SECTION.write(buf, section);
                section.getLight().writeBlockLight(buf);

                if (!section.getLight().hasSkyLight()) continue; // No sky light, we're done here.
                section.getLight().writeSkyLight(buf);

            }
            buf.readerIndex(0);
            Type.VAR_INT.writePrimitive(output, buf.readableBytes() + (chunk.isBiomeData() ? 256 : 0));
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        // Write biome data
        if (chunk.isBiomeData()) {
            for (int biome : chunk.getBiomeData()) {
                output.writeByte((byte) biome);
            }
        }
    }
}
