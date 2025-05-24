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

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class VarIntType extends Type<Integer> implements TypeConverter<Integer> {

    private static final int CONTINUE_BIT = 0x80;
    private static final int VALUE_BITS = 0x7F;
    private static final int MULTI_BYTE_BITS = ~VALUE_BITS;
    private static final int MAX_BYTES = 5;
    private static final int[] VAR_INT_LENGTHS = new int[65];

    static {
        // Copied from Velocity https://github.com/PaperMC/Velocity/blob/08a42b3723633ea5eb6b96c0bb42180f3c2b07eb/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java#L166
        for (int i = 0; i <= 32; ++i) {
            VAR_INT_LENGTHS[i] = (int) Math.ceil((31d - (i - 1)) / 7d);
        }
        VAR_INT_LENGTHS[32] = 1; // Special case for the number 0.
    }

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
    public void write(final Ops ops, final Integer integer) {
        Types.INT.write(ops, integer);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number number) {
            return number.intValue();
        } else if (o instanceof Boolean boo) {
            return boo ? 1 : 0;
        }
        throw new UnsupportedOperationException();
    }

    public static int varIntLength(final int value) {
        return VAR_INT_LENGTHS[Integer.numberOfLeadingZeros(value)];
    }
}
