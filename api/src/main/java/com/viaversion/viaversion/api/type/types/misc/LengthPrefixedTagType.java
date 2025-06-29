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
package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class LengthPrefixedTagType extends Type<Tag> {

    private final int maxLength;

    public LengthPrefixedTagType(int maxLength) {
        super(Tag.class);
        this.maxLength = maxLength;
    }

    @Override
    public Tag read(final ByteBuf buffer) {
        final int length = Types.VAR_INT.readPrimitive(buffer);
        if (length <= 0) {
            return null;
        }

        Preconditions.checkArgument(length <= maxLength,
            "Cannot receive tag longer than %s bytes (got %s bytes)", maxLength, length);

        return Types.TAG.read(buffer.readSlice(length));
    }

    @Override
    public void write(final ByteBuf buffer, final Tag tag) {
        if (tag == null) {
            Types.VAR_INT.writePrimitive(buffer, 0);
            return;
        }

        final ByteBuf tempBuf = buffer.alloc().buffer();
        try {
            Types.TAG.write(tempBuf, tag);

            Preconditions.checkArgument(tempBuf.readableBytes() <= maxLength,
                "Cannot send tag longer than %s bytes (got %s bytes)", maxLength, tempBuf.readableBytes());

            Types.VAR_INT.writePrimitive(buffer, tempBuf.readableBytes());
            buffer.writeBytes(tempBuf);
        } finally {
            tempBuf.release();
        }
    }
}
