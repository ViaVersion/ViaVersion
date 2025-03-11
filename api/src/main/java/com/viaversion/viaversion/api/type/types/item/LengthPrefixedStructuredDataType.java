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
package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wraps a data component codec, where data components are prefixed with their byte size (used in creative mode packets).
 */
public class LengthPrefixedStructuredDataType extends Type<StructuredData<?>> implements StructuredDataTypeBase {

    private final StructuredDataType wrapped;

    public LengthPrefixedStructuredDataType(final StructuredDataType dataType) {
        super(StructuredData.class);
        this.wrapped = dataType;
    }

    @Override
    public void write(final ByteBuf buffer, final StructuredData<?> value) {
        Types.VAR_INT.writePrimitive(buffer, value.id());

        // Same deal as with chunk data, except pre-calculating the size would be madness
        final ByteBuf tempBuf = buffer.alloc().buffer();
        try {
            value.write(tempBuf);
            Types.VAR_INT.writePrimitive(buffer, tempBuf.readableBytes());
            buffer.writeBytes(tempBuf);
        } finally {
            tempBuf.release();
        }
    }

    @Override
    public StructuredData<?> read(final ByteBuf buffer) {
        final int id = Types.VAR_INT.readPrimitive(buffer);
        final StructuredDataKey<?> key = key(id);
        if (key == null) {
            throw new IllegalArgumentException("No data component serializer found for id " + id);
        }

        final int size = Types.VAR_INT.readPrimitive(buffer);
        final ByteBuf slicedBuf = buffer.readSlice(size);
        return wrapped.readData(slicedBuf, key, id);
    }

    @Override
    public @Nullable StructuredDataKey<?> key(final int id) {
        return wrapped.key(id);
    }
}
