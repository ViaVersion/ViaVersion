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

public final class SuspiciousStewEffect {

    public static final Type<SuspiciousStewEffect> TYPE = new Type<SuspiciousStewEffect>(SuspiciousStewEffect.class) {
        @Override
        public SuspiciousStewEffect read(final ByteBuf buffer) {
            final int effect = Type.VAR_INT.readPrimitive(buffer);
            final int duration = Type.VAR_INT.readPrimitive(buffer);
            return new SuspiciousStewEffect(effect, duration);
        }

        @Override
        public void write(final ByteBuf buffer, final SuspiciousStewEffect value) {
            Type.VAR_INT.writePrimitive(buffer, value.effect);
            Type.VAR_INT.writePrimitive(buffer, value.duration);
        }
    };
    public static final Type<SuspiciousStewEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final int effect;
    private final int duration;

    public SuspiciousStewEffect(final int effect, final int duration) {
        this.effect = effect;
        this.duration = duration;
    }

    public int mobEffect() {
        return effect;
    }

    public int duration() {
        return duration;
    }
}
