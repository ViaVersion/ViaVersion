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
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicItem implements Item {
    private final Int2ObjectMap<Optional<StructuredData<?>>> data;
    private int identifier;
    private byte amount;

    public DynamicItem() {
        this(0, (byte) 0, new Int2ObjectOpenHashMap<>());
    }

    public DynamicItem(int identifier, byte amount, Int2ObjectMap<Optional<StructuredData<?>>> data) {
        this.identifier = identifier;
        this.amount = amount;
        this.data = data;
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
    public @Nullable CompoundTag tag() {
        return null;
    }

    @Override
    public void setTag(@Nullable CompoundTag tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Int2ObjectMap<Optional<StructuredData<?>>> itemData() {
        return data;
    }

    public void addData(StructuredData<?> data) {
        this.data.put(data.id(), Optional.of(data));
    }

    public void removeDefault(int id) {
        // Empty optional to override the Minecraft default
        this.data.put(id, Optional.empty());
    }

    @Override
    public Item copy() {
        return new DynamicItem(identifier, amount, data);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DynamicItem that = (DynamicItem) o;
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
}
