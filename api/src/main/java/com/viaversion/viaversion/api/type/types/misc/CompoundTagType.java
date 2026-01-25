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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Map;


/**
 * Compound tag type, functionally equivalent to {@link TagType} with an additional cast.
 * <p>
 * On the network, this is technically written as any tag, but almost always cast to and checked
 * as a CompoundTag, so we provide this type for convenience.
 */
public class CompoundTagType extends Type<CompoundTag> {

    private final int maxBytes;

    public CompoundTagType() {
        this(true);
    }

    public CompoundTagType(final boolean limitMaxBytes) {
        super(CompoundTag.class);
        this.maxBytes = limitMaxBytes ? NamedCompoundTagType.MAX_NBT_BYTES : Integer.MAX_VALUE;
    }

    @Override
    public CompoundTag read(final ByteBuf buffer) {
        try {
            return NamedCompoundTagType.read(buffer, this.maxBytes, false);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final ByteBuf buffer, final CompoundTag object) {
        try {
            NamedCompoundTagType.write(buffer, object, null);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final Ops ops, final CompoundTag value) {
        ops.writeMap(map -> {
            for (final Map.Entry<String, Tag> entry : value.entrySet()) {
                map.write(entry.getKey(), Types.TAG, entry.getValue());
            }
        });
    }

    public static final class OptionalCompoundTagType extends OptionalType<CompoundTag> {

        private OptionalCompoundTagType(final Type<CompoundTag> tagType) {
            super(tagType);
        }

        public static OptionalCompoundTagType type() {
            return new OptionalCompoundTagType(Types.COMPOUND_TAG);
        }

        public static OptionalCompoundTagType trustedType() {
            return new OptionalCompoundTagType(Types.TRUSTED_COMPOUND_TAG);
        }
    }
}
