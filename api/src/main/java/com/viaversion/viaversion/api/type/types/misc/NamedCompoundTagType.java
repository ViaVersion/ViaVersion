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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NamedCompoundTagType extends Type<CompoundTag> {

    public static final int MAX_NBT_BYTES = 2097152; // 2mb
    public static final int MAX_NESTING_LEVEL = 512;

    public NamedCompoundTagType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(final ByteBuf buffer) {
        try {
            return read(buffer, MAX_NBT_BYTES, true);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final ByteBuf buffer, final CompoundTag object) {
        try {
            write(buffer, object, "");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompoundTag read(final ByteBuf buffer, final int maxBytes, final boolean readName) throws IOException {
        final byte id = buffer.readByte();
        if (id == 0) {
            return null;
        }
        if (id != CompoundTag.ID) {
            throw new IOException(String.format("Expected root tag to be a CompoundTag, was %s", id));
        }

        if (readName) {
            buffer.skipBytes(buffer.readUnsignedShort());
        }

        final TagLimiter tagLimiter = TagLimiter.create(maxBytes, MAX_NESTING_LEVEL);
        return CompoundTag.read(new ByteBufInputStream(buffer), tagLimiter, 0);
    }

    public static void write(final ByteBuf buffer, final Tag tag, final @Nullable String name) throws IOException {
        if (tag == null) {
            buffer.writeByte(0);
            return;
        }

        final ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        out.writeByte(tag.getTagId());
        if (name != null) {
            out.writeUTF(name);
        }
        tag.write(out);
    }

    public static final class OptionalNamedCompoundTagType extends OptionalType<CompoundTag> {

        public OptionalNamedCompoundTagType() {
            super(Types.NAMED_COMPOUND_TAG);
        }
    }
}
