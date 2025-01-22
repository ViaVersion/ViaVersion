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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class EitherHolderType<T> extends Type<EitherHolder<T>> {
    private final HolderType<T> holderType;

    public EitherHolderType(final HolderType<T> holderType) {
        super(EitherHolder.class);
        this.holderType = holderType;
    }

    @Override
    public EitherHolder<T> read(final ByteBuf buffer) {
        return read(buffer, this.holderType);
    }

    @Override
    public void write(final ByteBuf buffer, final EitherHolder<T> object) {
        write(buffer, object, this.holderType);
    }

    public static <T> EitherHolder<T> read(final ByteBuf buffer, final HolderType<T> holderType) {
        if (buffer.readBoolean()) {
            return EitherHolder.of(holderType.read(buffer));
        }
        return EitherHolder.of(Types.STRING.read(buffer));
    }

    public static <T> void write(final ByteBuf buffer, final EitherHolder<T> object, final HolderType<T> holderType) {
        if (object.hasHolder()) {
            buffer.writeBoolean(true);
            holderType.write(buffer, object.holder());
        } else {
            buffer.writeBoolean(false);
            Types.STRING.write(buffer, object.key());
        }
    }
}
