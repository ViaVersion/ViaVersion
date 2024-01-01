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
package com.viaversion.viaversion.api.type.types.misc;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import java.io.IOException;


/**
 * Compound tag type, functionally equivalent to {@link TagType} with an additional cast.
 * <p>
 * On the network, this is technically written as any tag, but almost always cast to and checked
 * as a CompoundTag, so we provide this type for convenience.
 */
public class CompoundTagType extends Type<CompoundTag> {

    public CompoundTagType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(final ByteBuf buffer) throws IOException {
        return NamedCompoundTagType.read(buffer, false);
    }

    @Override
    public void write(final ByteBuf buffer, final CompoundTag object) throws IOException {
        NamedCompoundTagType.write(buffer, object, null);
    }

    public static final class OptionalCompoundTagType extends OptionalType<CompoundTag> {

        public OptionalCompoundTagType() {
            super(Type.COMPOUND_TAG);
        }
    }
}
