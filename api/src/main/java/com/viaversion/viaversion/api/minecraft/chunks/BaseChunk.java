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

//TODO Move methods from distinctly different versions to different objects/interfaces
public class BaseChunk implements Chunk {
    protected final int x;
    protected final int z;
    protected final boolean fullChunk;
    protected boolean ignoreOldLightData;
    protected BitSet chunkSectionBitSet;
    protected int bitmask;
    protected ChunkSection[] sections;
    protected int[] biomeData;
    protected CompoundTag heightMap;
    protected final List<CompoundTag> blockEntities;

    public BaseChunk(int x, int z, boolean fullChunk, boolean ignoreOldLightData, @Nullable BitSet chunkSectionBitSet,
                     ChunkSection[] sections, int @Nullable [] biomeData, @Nullable CompoundTag heightMap, List<CompoundTag> blockEntities) {
        this.x = x;
        this.z = z;
        this.fullChunk = fullChunk;
        this.ignoreOldLightData = ignoreOldLightData;
        this.chunkSectionBitSet = chunkSectionBitSet;
        this.sections = sections;
        this.biomeData = biomeData;
        this.heightMap = heightMap;
        this.blockEntities = blockEntities;
    }

    public BaseChunk(int x, int z, boolean fullChunk, boolean ignoreOldLightData, int bitmask, ChunkSection[] sections, int[] biomeData, CompoundTag heightMap, List<CompoundTag> blockEntities) {
        this(x, z, fullChunk, ignoreOldLightData, null, sections, biomeData, heightMap, blockEntities);
        this.bitmask = bitmask;
    }

    public BaseChunk(int x, int z, boolean fullChunk, boolean ignoreOldLightData, int bitmask, ChunkSection[] sections, int[] biomeData, List<CompoundTag> blockEntities) {
        this(x, z, fullChunk, ignoreOldLightData, bitmask, sections, biomeData, null, blockEntities);
    }

    @Override
    public boolean isBiomeData() {
        return biomeData != null;
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
        return fullChunk;
    }

    @Override
    public boolean isIgnoreOldLightData() {
        return ignoreOldLightData;
    }

    @Override
    public void setIgnoreOldLightData(boolean ignoreOldLightData) {
        this.ignoreOldLightData = ignoreOldLightData;
    }

    @Override
    public int getBitmask() {
        return bitmask;
    }

    @Override
    public void setBitmask(int bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public @Nullable BitSet getChunkMask() {
        return chunkSectionBitSet;
    }

    @Override
    public void setChunkMask(BitSet chunkSectionMask) {
        this.chunkSectionBitSet = chunkSectionMask;
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
        return biomeData;
    }

    @Override
    public void setBiomeData(int @Nullable [] biomeData) {
        this.biomeData = biomeData;
    }

    @Override
    public @Nullable CompoundTag getHeightMap() {
        return heightMap;
    }

    @Override
    public void setHeightMap(final CompoundTag heightMap) {
        this.heightMap = heightMap;
    }

    @Override
    public Heightmap[] heightmaps() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightmaps(final Heightmap[] heightmaps) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CompoundTag> getBlockEntities() {
        return blockEntities;
    }

    @Override
    public List<BlockEntity> blockEntities() {
        throw new UnsupportedOperationException();
    }
}
