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

import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class HolderSetType extends Type<HolderSet> {

    public HolderSetType() {
        super(HolderSet.class);
    }

    @Override
    public HolderSet read(final ByteBuf buffer) throws Exception {
        final int size = Type.VAR_INT.readPrimitive(buffer) - 1;
        if (size == -1) {
            final String tag = Type.STRING.read(buffer);
            return HolderSet.of(tag);
        }

        final int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = Type.VAR_INT.readPrimitive(buffer);
        }
        return HolderSet.of(values);
    }

    @Override
    public void write(final ByteBuf buffer, final HolderSet object) throws Exception {
        if (object.hasTagKey()) {
            Type.VAR_INT.writePrimitive(buffer, 0);
            Type.STRING.write(buffer, object.tagKey());
        } else {
            final int[] values = object.ids();
            Type.VAR_INT.writePrimitive(buffer, values.length + 1);
            for (final int value : values) {
                Type.VAR_INT.writePrimitive(buffer, value);
            }
        }
    }

    public static final class OptionalHolderSetType extends OptionalType<HolderSet> {

        public OptionalHolderSetType() {
            super(Type.HOLDER_SET);
        }
    }
}
