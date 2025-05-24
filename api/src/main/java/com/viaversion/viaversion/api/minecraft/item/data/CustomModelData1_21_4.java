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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record CustomModelData1_21_4(float[] floats, boolean[] booleans, String[] strings,
                                    int[] colors) implements Copyable {

    public static final Type<CustomModelData1_21_4> TYPE = new Type<>(CustomModelData1_21_4.class) {
        @Override
        public CustomModelData1_21_4 read(final ByteBuf buffer) {
            final float[] floats = Types.FLOAT_ARRAY_PRIMITIVE.read(buffer);
            final boolean[] booleans = Types.BOOLEAN_ARRAY_PRIMITIVE.read(buffer);
            final String[] strings = Types.STRING_ARRAY.read(buffer);
            final int[] colors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
            return new CustomModelData1_21_4(floats, booleans, strings, colors);
        }

        @Override
        public void write(final ByteBuf buffer, final CustomModelData1_21_4 value) {
            Types.FLOAT_ARRAY_PRIMITIVE.write(buffer, value.floats());
            Types.BOOLEAN_ARRAY_PRIMITIVE.write(buffer, value.booleans());
            Types.STRING_ARRAY.write(buffer, value.strings());
            Types.INT_ARRAY_PRIMITIVE.write(buffer, value.colors());
        }

        @Override
        public void write(final Ops ops, final CustomModelData1_21_4 value) {
            ops.writeMap(map -> map
                .writeOptional("floats", new ArrayType<>(Types.FLOAT), ArrayUtil.boxedArray(value.floats), new Float[0])
                .writeOptional("flags", new ArrayType<>(Types.BOOLEAN), ArrayUtil.boxedArray(value.booleans), new Boolean[0])
                .writeOptional("strings", Types.STRING_ARRAY, value.strings(), new String[0])
                .writeOptional("colors", new ArrayType<>(Types.INT), ArrayUtil.boxedArray(value.colors), new Integer[0]));
        }
    };

    @Override
    public CustomModelData1_21_4 copy() {
        return new CustomModelData1_21_4(Copyable.copy(floats), Copyable.copy(booleans), Copyable.copy(strings), Copyable.copy(colors));
    }
}
