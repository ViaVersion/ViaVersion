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

package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;

public class IdAndData {

    private int id;
    private byte data;

    public IdAndData(int id, int data) {
        Preconditions.checkArgument(data >= 0 && data <= 15, "Data has to be between 0 and 15: (id: " + id + " data: " + data + ")");
        this.id = id;
        this.data = (byte) data;
    }

    public static int getId(final int compressedData) {
        return compressedData >> 4;
    }

    public static int getData(final int compressedData) {
        return compressedData & 15;
    }

    public static int toCompressedData(final int id) {
        return id << 4;
    }

    public static int removeData(final int data) {
        return data & ~15;
    }

    public static IdAndData fromCompressedData(final int compressedData) {
        return new IdAndData(compressedData >> 4, compressedData & 15);
    }

    public static int toCompressedData(final int id, final int data) {
        return (id << 4) | (data & 15);
    }

    public int toCompressedData() {
        return toCompressedData(id, data);
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public byte getData() {
        return data;
    }

    public void setData(final byte data) {
        this.data = data;
    }
}
