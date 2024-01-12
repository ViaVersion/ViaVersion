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
package com.viaversion.viaversion.api.type;

import io.netty.buffer.ByteBuf;

@Deprecated
public abstract class PartialType<T, X> extends Type<T> {
    private final X param;

    protected PartialType(X param, Class<T> type) {
        super(type);
        this.param = param;
    }

    protected PartialType(X param, String name, Class<T> type) {
        super(name, type);
        this.param = param;
    }

    public abstract T read(ByteBuf buffer, X param) throws Exception;

    public abstract void write(ByteBuf buffer, X param, T object) throws Exception;

    @Override
    public final T read(ByteBuf buffer) throws Exception {
        return read(buffer, this.param);
    }

    @Override
    public final void write(ByteBuf buffer, T object) throws Exception {
        write(buffer, this.param, object);
    }
}
