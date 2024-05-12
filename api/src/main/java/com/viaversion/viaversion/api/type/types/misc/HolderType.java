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

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public abstract class HolderType<T> extends Type<Holder<T>> {

    protected HolderType() {
        super(Holder.class);
    }

    @Override
    public Holder<T> read(final ByteBuf buffer) {
        final int id = Types.VAR_INT.readPrimitive(buffer) - 1; // Normalize id
        if (id == -1) {
            return Holder.of(readDirect(buffer));
        }
        return Holder.of(id);
    }

    @Override
    public void write(final ByteBuf buffer, final Holder<T> object) {
        if (object.hasId()) {
            Types.VAR_INT.writePrimitive(buffer, object.id() + 1); // Normalize id
        } else {
            Types.VAR_INT.writePrimitive(buffer, 0);
            writeDirect(buffer, object.value());
        }
    }

    public abstract T readDirect(final ByteBuf buffer);

    public abstract void writeDirect(final ByteBuf buffer, final T object);
}
