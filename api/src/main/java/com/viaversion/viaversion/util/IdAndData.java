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
import java.util.Objects;

public class IdAndData {

    private int id;
    private byte data;

    public IdAndData(final int id) {
        this.id = id;
        this.data = -1;
    }

    public IdAndData(final int id, final int data) {
        Preconditions.checkArgument(data >= 0 && data <= 15, "Data has to be between 0 and 15: (id: " + id + " data: " + data + ")");
        this.id = id;
        this.data = (byte) data;
    }

    public static int getId(final int rawData) {
        return rawData >> 4;
    }

    public static int getData(final int rawData) {
        return rawData & 15;
    }

    public static int toRawData(final int id) {
        return id << 4;
    }

    public static int removeData(final int data) {
        return data & ~15;
    }

    public static IdAndData fromRawData(final int rawData) {
        return new IdAndData(rawData >> 4, rawData & 15);
    }

    public static int toRawData(final int id, final int data) {
        return (id << 4) | (data & 15);
    }

    public int toRawData() {
        return toRawData(id, data);
    }

    public IdAndData withData(final int data) {
        return new IdAndData(this.id, data);
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

    public void setData(final int data) {
        this.data = (byte) data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdAndData idAndData = (IdAndData) o;
        return id == idAndData.id && data == idAndData.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }

    @Override
    public String toString() {
        return "IdAndData{" +
            "id=" + id +
            ", data=" + data +
            '}';
    }
}
