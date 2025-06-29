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
package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.type.Type;
import java.util.function.Consumer;

public class ThrowingOps implements Ops {

    @Override
    public CodecContext context() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeByte(final byte b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBytes(final byte[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBoolean(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeShort(final short s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeString(final CharSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInt(final int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLong(final long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloat(final float f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDouble(final double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInts(final int[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLongs(final long[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeList(final Consumer<ListSerializer> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeMap(final Consumer<MapSerializer> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> void write(final Type<V> type, final V value) {
        throw new UnsupportedOperationException();
    }
}
