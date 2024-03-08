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

public interface Holder<T> {

    /**
     * Returns an indirect id holder.
     *
     * @param id  the id
     * @param <T> the type of the value
     * @return a new holder with the given id
     * @throws IllegalArgumentException if the id is negative
     */
    static <T> Holder<T> of(final int id) {
        return new HolderImpl<>(id);
    }

    /**
     * Returns a direct value holder.
     *
     * @param value the value
     * @param <T>   the type of the value
     * @return a new direct holder
     */
    static <T> Holder<T> of(final T value) {
        return new HolderImpl<>(value);
    }

    /**
     * Returns true if this holder is backed by a direct value.
     *
     * @return true if the holder is direct
     * @see #hasId()
     */
    boolean isDirect();

    /**
     * Returns true if this holder has an id.
     *
     * @return true if this holder has an id
     * @see #isDirect()
     */
    boolean hasId();

    /**
     * Returns the value of this holder.
     *
     * @return the value of this holder
     * @throws IllegalArgumentException if this holder is not direct
     * @see #isDirect()
     */
    T value();

    /**
     * Returns the id of this holder, or -1 if this holder is direct.
     *
     * @return the id of this holder, or -1 if this holder is direct
     * @see #hasId()
     */
    int id();
}
