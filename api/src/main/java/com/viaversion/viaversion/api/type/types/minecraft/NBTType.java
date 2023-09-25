/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types.minecraft;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.limiter.TagLimiter;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NBTType extends Type<CompoundTag> {

    private static final int MAX_NBT_BYTES = 2097152; // 2mb
    private static final int MAX_NESTING_LEVEL = 512;

    public NBTType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(final ByteBuf buffer) throws Exception {
        return read(buffer, true);
    }

    @Override
    public void write(final ByteBuf buffer, final CompoundTag object) throws Exception {
        write(buffer, object, "");
    }

    public static CompoundTag read(final ByteBuf buffer, final boolean readName) throws Exception {
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

        final TagLimiter tagLimiter = TagLimiter.create(MAX_NBT_BYTES, MAX_NESTING_LEVEL);
        final CompoundTag tag = new CompoundTag();
        tag.read(new ByteBufInputStream(buffer), tagLimiter);
        return tag;
    }

    public static void write(final ByteBuf buffer, final CompoundTag tag, final @Nullable String name) throws Exception {
        if (tag == null) {
            buffer.writeByte(0);
            return;
        }

        final ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        out.writeByte(CompoundTag.ID);
        if (name != null) {
            out.writeUTF(name);
        }
        tag.write(out);
    }
}
