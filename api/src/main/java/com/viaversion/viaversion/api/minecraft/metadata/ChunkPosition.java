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
package com.viaversion.viaversion.api.minecraft.metadata;

import java.util.Objects;

public final class ChunkPosition {
    private final int chunkX;
    private final int chunkZ;

    public ChunkPosition(final int chunkX, final int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public ChunkPosition(final long chunkKey) {
        this.chunkX = (int) chunkKey;
        this.chunkZ = (int) (chunkKey >> 32);
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    /**
     * Returns a long key for this chunk position.
     *
     * @return the chunk key
     */
    public long chunkKey() {
        return chunkKey(chunkX, chunkZ);
    }

    /**
     * Returns a long key for the given chunk coordinates.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return the chunk key
     */
    public static long chunkKey(final int chunkX, final int chunkZ) {
        return (long) chunkX & 0xffffffffL | ((long) chunkZ & 0xffffffffL) << 32;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChunkPosition that = (ChunkPosition) o;
        return chunkX == that.chunkX && chunkZ == that.chunkZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return "ChunkPosition{" +
                "chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }
}
