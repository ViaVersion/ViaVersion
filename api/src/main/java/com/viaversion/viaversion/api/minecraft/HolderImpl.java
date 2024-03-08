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
package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;

final class HolderImpl<T> implements Holder<T> {

    private final T value;
    private final int id;

    HolderImpl(final int id) {
        Preconditions.checkArgument(id >= 0, "id cannot be negative");
        this.value = null;
        this.id = id;
    }

    HolderImpl(final T value) {
        this.value = value;
        this.id = -1;
    }

    @Override
    public boolean isDirect() {
        return id == -1;
    }

    @Override
    public boolean hasId() {
        return id != -1;
    }

    @Override
    public T value() {
        Preconditions.checkArgument(isDirect(), "Holder is not direct");
        return value;
    }

    @Override
    public int id() {
        return id;
    }
}
