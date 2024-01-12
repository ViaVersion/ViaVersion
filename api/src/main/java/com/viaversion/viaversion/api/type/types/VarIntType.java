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
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class VarIntType extends Type<Integer> implements TypeConverter<Integer> {

    private static final int CONTINUE_BIT = 0x80;
    private static final int VALUE_BITS = 0x7F;
    private static final int MULTI_BYTE_BITS = ~VALUE_BITS;
    private static final int MAX_BYTES = 5;

    public VarIntType() {
        super("VarInt", Integer.class);
    }

    public int readPrimitive(ByteBuf buffer) {
        int value = 0;
        int bytes = 0;
        byte in;
        do {
            in = buffer.readByte();
            value |= (in & VALUE_BITS) << (bytes++ * 7);
            if (bytes > MAX_BYTES) {
                throw new RuntimeException("VarInt too big");
            }

        } while ((in & CONTINUE_BIT) == CONTINUE_BIT);
        return value;
    }

    public void writePrimitive(ByteBuf buffer, int value) {
        while ((value & MULTI_BYTE_BITS) != 0) {
            buffer.writeByte((value & VALUE_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }

        buffer.writeByte(value);
    }

    /**
     * @deprecated use {@link #readPrimitive(ByteBuf)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public Integer read(ByteBuf buffer) {
        return readPrimitive(buffer);
    }

    /**
     * @deprecated use {@link #writePrimitive(ByteBuf, int)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public void write(ByteBuf buffer, Integer object) {
        writePrimitive(buffer, object);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Integer) o;
    }
}