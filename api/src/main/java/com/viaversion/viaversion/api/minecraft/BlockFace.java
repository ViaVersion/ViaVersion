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

import java.util.EnumMap;
import java.util.Map;

public enum BlockFace {
    NORTH((byte) 0, (byte) 0, (byte) -1, EnumAxis.Z),
    SOUTH((byte) 0, (byte) 0, (byte) 1, EnumAxis.Z),
    EAST((byte) 1, (byte) 0, (byte) 0, EnumAxis.X),
    WEST((byte) -1, (byte) 0, (byte) 0, EnumAxis.X),
    TOP((byte) 0, (byte) 1, (byte) 0, EnumAxis.Y),
    BOTTOM((byte) 0, (byte) -1, (byte) 0, EnumAxis.Y);

    private static final Map<BlockFace, BlockFace> opposites = new EnumMap<>(BlockFace.class);

    static {
        opposites.put(BlockFace.NORTH, BlockFace.SOUTH);
        opposites.put(BlockFace.SOUTH, BlockFace.NORTH);
        opposites.put(BlockFace.EAST, BlockFace.WEST);
        opposites.put(BlockFace.WEST, BlockFace.EAST);
        opposites.put(BlockFace.TOP, BlockFace.BOTTOM);
        opposites.put(BlockFace.BOTTOM, BlockFace.TOP);
    }

    private final byte modX;
    private final byte modY;
    private final byte modZ;
    private final EnumAxis axis;

    BlockFace(byte modX, byte modY, byte modZ, EnumAxis axis) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
        this.axis = axis;
    }

    public BlockFace opposite() {
        return opposites.get(this);
    }

    public byte modX() {
        return modX;
    }

    public byte modY() {
        return modY;
    }

    public byte modZ() {
        return modZ;
    }

    public EnumAxis axis() {
        return axis;
    }

    public enum EnumAxis {
        X, Y, Z
    }
}
