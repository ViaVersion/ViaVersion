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
package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;
import java.util.Objects;

public class EitherImpl<X, Y> implements Either<X, Y> {
    private final X left;
    private final Y right;

    protected EitherImpl(final X left, final Y value) {
        this.left = left;
        this.right = value;
        Preconditions.checkArgument(left == null || value == null, "Either.left and Either.right are both present");
        Preconditions.checkArgument(left != null || value != null, "Either.left and Either.right are both null");
    }

    @Override
    public boolean isLeft() {
        return left != null;
    }

    @Override
    public boolean isRight() {
        return right != null;
    }

    @Override
    public X left() {
        return left;
    }

    @Override
    public Y right() {
        return right;
    }

    @Override
    public String toString() {
        return "Either{" + left + ", " + right + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EitherImpl<?, ?> pair = (EitherImpl<?, ?>) o;
        if (!Objects.equals(left, pair.left)) return false;
        return Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
