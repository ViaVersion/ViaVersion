/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class RemainingBytesType extends Type<byte[]> {
    private final int maxLength;

    public RemainingBytesType() {
        this(-1);
    }

    public RemainingBytesType(final int maxLength) {
        super(byte[].class);
        this.maxLength = maxLength;
    }

    @Override
    public byte[] read(final ByteBuf buffer) {
        final int bytes = buffer.readableBytes();
        if (maxLength != -1 && bytes > maxLength) {
            throw new RuntimeException("Remaining bytes cannot be longer than " + maxLength + " (got " + bytes + ")");
        }

        final byte[] array = new byte[bytes];
        buffer.readBytes(array);
        return array;
    }

    @Override
    public void write(final ByteBuf buffer, final byte[] object) {
        if (maxLength != -1 && object.length > maxLength) {
            throw new RuntimeException("Remaining bytes cannot be longer than " + maxLength + " (got " + object.length + ")");
        }

        buffer.writeBytes(object);
    }
}
