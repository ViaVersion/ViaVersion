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
package com.viaversion.viaversion.api.minecraft.chunks;

import java.util.Arrays;

public class NibbleArray {
    private final byte[] handle;

    public NibbleArray(int length) {
        if (length == 0 || length % 2 != 0) {
            throw new IllegalArgumentException("Length of nibble array must be a positive number dividable by 2!");
        }

        this.handle = new byte[length / 2];
    }

    public NibbleArray(byte[] handle) {
        if (handle.length == 0 || handle.length % 2 != 0) {
            throw new IllegalArgumentException("Length of nibble array must be a positive number dividable by 2!");
        }

        this.handle = handle;
    }

    /**
     * Get the value at a desired X, Y, Z
     *
     * @param x Block X
     * @param y Block Y
     * @param z Block Z
     * @return The value at the given XYZ
     */
    public byte get(int x, int y, int z) {
        return get(ChunkSection.index(x, y, z));
    }

    /**
     * Get the value at an index
     *
     * @param index The index to lookup
     * @return The value at that index.
     */
    public byte get(int index) {
        byte value = handle[index / 2];
        if (index % 2 == 0) {
            return (byte) (value & 0xF);
        } else {
            return (byte) ((value >> 4) & 0xF);
        }
    }

    /**
     * Set the value based on an x, y, z
     *
     * @param x     Block X
     * @param y     Block Y
     * @param z     Block Z
     * @param value Desired Value
     */
    public void set(int x, int y, int z, int value) {
        set(ChunkSection.index(x, y, z), value);
    }

    /**
     * Set a value at an index
     *
     * @param index The index to set the value at.
     * @param value The desired value
     */
    public void set(int index, int value) {
        if (index % 2 == 0) {
            index /= 2;
            handle[index] = (byte) ((handle[index] & 0xF0) | (value & 0xF));
        } else {
            index /= 2;
            handle[index] = (byte) ((handle[index] & 0xF) | ((value & 0xF) << 4));
        }
    }

    /**
     * The size of this nibble
     *
     * @return The size as an int of the nibble
     */
    public int size() {
        return handle.length * 2;
    }

    /**
     * Get the actual number of bytes
     *
     * @return The number of bytes based on the handle.
     */
    public int actualSize() {
        return handle.length;
    }

    /**
     * Fill the array with a value
     *
     * @param value Value to fill with
     */
    public void fill(byte value) {
        value &= 0xF; // Max nibble size (= 16)
        Arrays.fill(handle, (byte) ((value << 4) | value));
    }

    /**
     * Get the byte array behind this nibble
     *
     * @return The byte array
     */
    public byte[] getHandle() {
        return handle;
    }

    /**
     * Copy a byte array into this nibble
     *
     * @param handle The byte array to copy in.
     */
    public void setHandle(byte[] handle) {
        if (handle.length != this.handle.length) {
            throw new IllegalArgumentException("Length of handle must equal to size of nibble array!");
        }

        System.arraycopy(handle, 0, this.handle, 0, handle.length);
    }
}
