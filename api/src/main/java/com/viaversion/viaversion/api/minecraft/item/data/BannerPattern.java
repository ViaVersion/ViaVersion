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
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public final class BannerPattern {

    public static final Type<BannerPattern> TYPE = new Type<BannerPattern>(BannerPattern.class) {
        @Override
        public BannerPattern read(final ByteBuf buffer) throws Exception {
            final int pattern = Type.VAR_INT.readPrimitive(buffer);
            final int color = Type.VAR_INT.readPrimitive(buffer);
            return new BannerPattern(pattern, color);
        }

        @Override
        public void write(final ByteBuf buffer, final BannerPattern value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.pattern);
            Type.VAR_INT.writePrimitive(buffer, value.color);
        }
    };
    public static final Type<BannerPattern[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final int pattern;
    private final int color;

    public BannerPattern(final int pattern, final int color) {
        this.pattern = pattern;
        this.color = color;
    }

    public int pattern() {
        return this.pattern;
    }

    public int color() {
        return this.color;
    }
}
