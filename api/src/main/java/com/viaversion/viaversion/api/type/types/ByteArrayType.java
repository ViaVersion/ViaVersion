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

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class ByteArrayType extends Type<byte[]> {

    private final int length;
    private final int maxLength;

    public ByteArrayType(final int length) {
        this(length, -1);
    }

    public ByteArrayType() {
        this(-1, -1);
    }

    private ByteArrayType(final int length, final int maxLength) {
        super(byte[].class);
        this.length = length;
        this.maxLength = maxLength;
    }

    /**
     * Returns a length-prefixed byte array type that rejects arrays longer than the given maximum.
     *
     * @param maxLength maximum array length
     * @return length-prefixed byte array type with the given maximum length
     */
    public static ByteArrayType withMaxLength(final int maxLength) {
        return new ByteArrayType(-1, maxLength);
    }

    @Override
    public void write(final ByteBuf buffer, final byte[] object) {
        if (this.length != -1) {
            Preconditions.checkArgument(length == object.length, "Length does not match expected length");
        } else {
            checkMaxLength(object.length);
            Types.VAR_INT.writePrimitive(buffer, object.length);
        }
        buffer.writeBytes(object);
    }

    @Override
    public byte[] read(final ByteBuf buffer) {
        final int length = this.length == -1 ? checkMaxLength(Types.VAR_INT.readPrimitive(buffer)) : this.length;
        Preconditions.checkArgument(buffer.isReadable(length), "Length is larger than readable bytes");
        final byte[] array = new byte[length];
        buffer.readBytes(array);
        return array;
    }

    private int checkMaxLength(final int length) {
        if (maxLength != -1 && length > maxLength) {
            throw new IllegalArgumentException("Byte array length " + length + " is longer than maximum " + maxLength);
        }
        return length;
    }

    @Override
    public void write(final Ops ops, final byte[] value) {
        ops.writeBytes(value);
    }

    public static final class OptionalByteArrayType extends OptionalType<byte[]> {

        public OptionalByteArrayType() {
            super(Types.BYTE_ARRAY_PRIMITIVE);
        }

        public OptionalByteArrayType(final int length) {
            super(new ByteArrayType(length));
        }
    }
}
