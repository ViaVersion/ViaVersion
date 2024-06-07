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
package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredItem implements Item {
    private final StructuredDataContainer data;
    private int identifier;
    private int amount;

    public StructuredItem(final int identifier, final int amount) {
        this(identifier, amount, new StructuredDataContainer());
    }

    public StructuredItem(final int identifier, final int amount, final StructuredDataContainer data) {
        this.identifier = identifier;
        this.amount = amount;
        this.data = data;
    }

    public static StructuredItem empty() {
        // Data is mutable, create a new item
        return new StructuredItem(0, 0);
    }

    public static StructuredItem[] emptyArray(final int size) {
        final StructuredItem[] items = new StructuredItem[size];
        for (int i = 0; i < items.length; i++) {
            items[i] = empty();
        }
        return items;
    }

    @Override
    public int identifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
    }

    @Override
    public int amount() {
        return amount;
    }

    @Override
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    @Override
    public @Nullable CompoundTag tag() {
        return null;
    }

    @Override
    public void setTag(@Nullable final CompoundTag tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StructuredDataContainer dataContainer() {
        return data;
    }

    @Override
    public StructuredItem copy() {
        return new StructuredItem(identifier, amount, data.copy());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StructuredItem that = (StructuredItem) o;
        if (identifier != that.identifier) return false;
        if (amount != that.amount) return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + identifier;
        result = 31 * result + amount;
        return result;
    }

    @Override
    public String toString() {
        return "StructuredItem{" +
            "data=" + data +
            ", identifier=" + identifier +
            ", amount=" + amount +
            '}';
    }
}