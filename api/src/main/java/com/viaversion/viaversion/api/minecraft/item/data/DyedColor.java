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
import io.netty.buffer.ByteBuf;

public record DyedColor(int rgb, boolean showInTooltip) {

    public DyedColor(final int rgb) {
        this(rgb, true);
    }

    public static final Type<DyedColor> TYPE1_20_5 = new Type<>(DyedColor.class) {
        @Override
        public DyedColor read(final ByteBuf buffer) {
            final int rgb = buffer.readInt();
            final boolean showInTooltip = buffer.readBoolean();
            return new DyedColor(rgb, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final DyedColor value) {
            buffer.writeInt(value.rgb);
            buffer.writeBoolean(value.showInTooltip);
        }
    };
    public static final Type<DyedColor> TYPE1_21_5 = new Type<>(DyedColor.class) {
        @Override
        public DyedColor read(final ByteBuf buffer) {
            final int rgb = buffer.readInt();
            return new DyedColor(rgb);
        }

        @Override
        public void write(final ByteBuf buffer, final DyedColor value) {
            buffer.writeInt(value.rgb);
        }

        @Override
        public void write(final Ops ops, final DyedColor dyedColor) {
            ops.write(Types.INT, dyedColor.rgb);
        }
    };
}
