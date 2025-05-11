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
package com.viaversion.viaversion.api.minecraft.item;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class HashedStructuredItem implements HashedItem {
    private final Int2IntMap dataHashes;
    private final IntSet removedData;
    private int identifier;
    private int amount;

    public HashedStructuredItem(final int identifier, final int amount) {
        this(identifier, amount, new Int2IntOpenHashMap(0), new IntOpenHashSet(0));
    }

    public HashedStructuredItem(final int identifier, final int amount, final Int2IntMap dataHashes, final IntSet removedData) {
        this.identifier = identifier;
        this.amount = amount;
        this.dataHashes = dataHashes;
        this.removedData = removedData;
    }

    public static HashedStructuredItem empty() {
        // Data is mutable, create a new item
        return new HashedStructuredItem(0, 0);
    }

    @Override
    public int identifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
    }

    @Override
    public int amount() {
        return this.amount;
    }

    @Override
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    @Override
    public Int2IntMap dataHashesById() {
        return this.dataHashes;
    }

    @Override
    public IntSet removedDataIds() {
        return this.removedData;
    }

    @Override
    public HashedItem copy() {
        return new HashedStructuredItem(identifier, amount, new Int2IntOpenHashMap(dataHashes), new IntOpenHashSet(removedData));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HashedStructuredItem that = (HashedStructuredItem) o;
        if (identifier != that.identifier) return false;
        if (amount != that.amount) return false;
        return dataHashes.equals(that.dataHashes) && removedData.equals(that.removedData);
    }

    @Override
    public int hashCode() {
        int result = dataHashes.hashCode();
        result = 31 * result + removedData.hashCode();
        result = 31 * result + identifier;
        result = 31 * result + amount;
        return result;
    }

    @Override
    public String toString() {
        return "HashedStructuredItem{" +
            "data=Hashes" + dataHashes +
            ", removedData=" + removedData +
            ", identifier=" + identifier +
            ", amount=" + amount +
            '}';
    }
}
