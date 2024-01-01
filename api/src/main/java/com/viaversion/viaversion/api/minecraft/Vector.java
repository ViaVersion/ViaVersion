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
package com.viaversion.viaversion.api.minecraft;

public class Vector {
    private int blockX;
    private int blockY;
    private int blockZ;

    public Vector(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public int blockX() {
        return blockX;
    }

    public int blockY() {
        return blockY;
    }

    public int blockZ() {
        return blockZ;
    }

    @Deprecated/*(forRemoval=true)*/
    public int getBlockX() {
        return blockX;
    }

    @Deprecated/*(forRemoval=true)*/
    public int getBlockY() {
        return blockY;
    }

    @Deprecated/*(forRemoval=true)*/
    public int getBlockZ() {
        return blockZ;
    }

    @Deprecated/*(forRemoval=true)*/
    public void setBlockX(int blockX) {
        this.blockX = blockX;
    }

    @Deprecated/*(forRemoval=true)*/
    public void setBlockY(int blockY) {
        this.blockY = blockY;
    }

    @Deprecated/*(forRemoval=true)*/
    public void setBlockZ(int blockZ) {
        this.blockZ = blockZ;
    }
}
