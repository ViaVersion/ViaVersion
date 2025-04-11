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
package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;

public class BlockChangeRecord1_16_2 implements BlockChangeRecord {
    private final byte sectionX;
    private final byte sectionY;
    private final byte sectionZ;
    private int blockId;

    public BlockChangeRecord1_16_2(byte sectionX, byte sectionY, byte sectionZ, int blockId) {
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;
        this.blockId = blockId;
    }

    public BlockChangeRecord1_16_2(int sectionX, int sectionY, int sectionZ, int blockId) {
        this((byte) sectionX, (byte) sectionY, (byte) sectionZ, blockId);
    }

    @Override
    public byte getSectionX() {
        return sectionX;
    }

    @Override
    public byte getSectionY() {
        return sectionY;
    }

    @Override
    public byte getSectionZ() {
        return sectionZ;
    }

    @Override
    public short getY(int chunkSectionY) {
        Preconditions.checkArgument(chunkSectionY >= 0, "Invalid chunkSectionY: %s", chunkSectionY);
        return (short) ((chunkSectionY << 4) + sectionY);
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
