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
package com.viaversion.viaversion.api.minecraft.chunks;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import java.util.BitSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Chunk1_21_5 implements Chunk {
    protected final int x;
    protected final int z;
    protected ChunkSection[] sections;
    protected Heightmap[] heightmaps;
    protected final List<BlockEntity> blockEntities;

    public Chunk1_21_5(int x, int z, ChunkSection[] sections, Heightmap[] heightmaps, List<BlockEntity> blockEntities) {
        this.x = x;
        this.z = z;
        this.sections = sections;
        this.heightmaps = heightmaps;
        this.blockEntities = blockEntities;
    }

    @Override
    public boolean isBiomeData() {
        return false;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public boolean isFullChunk() {
        return true;
    }

    @Override
    public boolean isIgnoreOldLightData() {
        return false;
    }

    @Override
    public void setIgnoreOldLightData(boolean ignoreOldLightData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBitmask() {
        return -1;
    }

    @Override
    public void setBitmask(int bitmask) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable BitSet getChunkMask() {
        return null;
    }

    @Override
    public void setChunkMask(BitSet chunkSectionMask) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkSection[] getSections() {
        return sections;
    }

    @Override
    public void setSections(ChunkSection[] sections) {
        this.sections = sections;
    }

    @Override
    public int @Nullable [] getBiomeData() {
        return null;
    }

    @Override
    public void setBiomeData(int @Nullable [] biomeData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable CompoundTag getHeightMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightMap(final CompoundTag heightMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Heightmap[] heightmaps() {
        return heightmaps;
    }

    @Override
    public void setHeightmaps(final Heightmap[] heightmaps) {
        this.heightmaps = heightmaps;
    }

    @Override
    public List<CompoundTag> getBlockEntities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BlockEntity> blockEntities() {
        return blockEntities;
    }
}
