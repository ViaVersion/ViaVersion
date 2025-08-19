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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class UnsignedIntType extends Type<Long> implements TypeConverter<Long> {
    public static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;

    public UnsignedIntType() {
        super(Long.class);
    }

    @Override
    public Long read(ByteBuf buffer) {
        return buffer.readUnsignedInt();
    }

    public long readPrimitive(ByteBuf buffer) {
        return buffer.readUnsignedInt();
    }

    @Override
    public void write(ByteBuf buffer, Long l) {
        buffer.writeInt(l.intValue());
    }

    public void writePrimitive(ByteBuf buffer, long l) {
        buffer.writeInt((int) l);
    }

    @Override
    public Long from(Object o) {
        if (o instanceof Number number) {
            return number.longValue();
        } else if (o instanceof Boolean boo) {
            return boo ? 1L : 0L;
        }
        throw new UnsupportedOperationException();
    }
}
