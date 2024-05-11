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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record MapDecoration(String type, double x, double z, float rotation) {

    public static final Type<MapDecoration> TYPE = new Type<>(MapDecoration.class) {
        @Override
        public MapDecoration read(final ByteBuf buffer) {
            final String type = Types.STRING.read(buffer);
            final double x = Types.DOUBLE.readPrimitive(buffer);
            final double z = Types.DOUBLE.readPrimitive(buffer);
            final float rotation = Types.FLOAT.readPrimitive(buffer);
            return new MapDecoration(type, x, z, rotation);
        }

        @Override
        public void write(final ByteBuf buffer, final MapDecoration value) {
            Types.STRING.write(buffer, value.type);
            buffer.writeDouble(value.x);
            buffer.writeDouble(value.z);
            buffer.writeFloat(value.rotation);
        }
    };
}
