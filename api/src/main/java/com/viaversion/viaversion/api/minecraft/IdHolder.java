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
package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.function.Function;

record IdHolder<T>(int id) implements Holder<T> {

    IdHolder {
        Preconditions.checkArgument(id >= 0, "id cannot be negative");
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public boolean hasId() {
        return true;
    }

    @Override
    public T value() {
        throw new IllegalArgumentException("Holder is not direct");
    }

    @Override
    public Holder<T> updateId(final Int2IntFunction rewriteFunction) {
        final int rewrittenId = rewriteFunction.applyAsInt(id);
        if (rewrittenId == id) {
            return this;
        }
        if (rewrittenId == -1) {
            throw new IllegalArgumentException("Received invalid id in updateId");
        }
        return Holder.of(rewrittenId);
    }

    @Override
    public Holder<T> updateValue(final Function<T, T> rewriteFunction) {
        return this;
    }
}
