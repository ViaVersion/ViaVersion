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
package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class LongArrayType extends Type<long[]> {

    private final int length;

    public LongArrayType(final int length) {
        super(long[].class);
        this.length = length;
    }

    public LongArrayType() {
        this(-1);
    }

    @Override
    public long[] read(final ByteBuf buffer) {
        final int length = this.length == -1 ? Types.VAR_INT.readPrimitive(buffer) : this.length;
        Preconditions.checkArgument(buffer.isReadable(length), "Length is fewer than readable bytes");
        return readFixedLength(buffer, length);
    }

    public static long[] readFixedLength(final ByteBuf buffer, final int expectedLength) {
        final long[] value = new long[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            value[i] = buffer.readLong();
        }
        return value;
    }

    @Override
    public void write(final ByteBuf buffer, final long[] object) {
        if (this.length != -1) {
            Preconditions.checkArgument(length == object.length, "Length does not match expected length");
        } else {
            Types.VAR_INT.writePrimitive(buffer, object.length);
        }
        writeFixedLength(buffer, object);
    }

    public static void writeFixedLength(final ByteBuf buffer, final long[] object) {
        for (final long l : object) {
            buffer.writeLong(l);
        }
    }
}
