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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.annotations.SerializedName;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DataItem implements Item {
    @SerializedName(value = "identifier", alternate = "id")
    private int identifier;
    private byte amount;
    private short data;
    private CompoundTag tag;

    public DataItem() {
    }

    public DataItem(int identifier, byte amount, short data, @Nullable CompoundTag tag) {
        this.identifier = identifier;
        this.amount = amount;
        this.data = data;
        this.tag = tag;
    }

    public DataItem(Item toCopy) {
        this(toCopy.identifier(), (byte) toCopy.amount(), toCopy.data(), toCopy.tag());
    }

    @Override
    public int identifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public int amount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        if (amount > Byte.MAX_VALUE || amount < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid item amount: " + amount);
        }
        this.amount = (byte) amount;
    }

    @Override
    public short data() {
        return data;
    }

    @Override
    public void setData(short data) {
        this.data = data;
    }

    @Override
    public @Nullable CompoundTag tag() {
        return tag;
    }

    @Override
    public void setTag(@Nullable CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    public StructuredDataContainer structuredData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item copy() {
        return new DataItem(identifier, amount, data, tag);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem item = (DataItem) o;
        if (identifier != item.identifier) return false;
        if (amount != item.amount) return false;
        if (data != item.data) return false;
        return Objects.equals(tag, item.tag);
    }

    @Override
    public int hashCode() {
        int result = identifier;
        result = 31 * result + (int) amount;
        result = 31 * result + (int) data;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
            "identifier=" + identifier +
            ", amount=" + amount +
            ", data=" + data +
            ", tag=" + tag +
            '}';
    }
}
