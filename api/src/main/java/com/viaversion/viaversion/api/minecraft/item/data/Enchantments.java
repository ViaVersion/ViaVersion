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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class Enchantments {

    public static final Type<Enchantments> TYPE = new Type<Enchantments>(Enchantments.class) {
        @Override
        public Enchantments read(final ByteBuf buffer) {
            final Int2IntMap enchantments = new Int2IntOpenHashMap();
            final int size = Type.VAR_INT.readPrimitive(buffer);
            for (int i = 0; i < size; i++) {
                final int id = Type.VAR_INT.readPrimitive(buffer);
                final int level = Type.VAR_INT.readPrimitive(buffer);
                enchantments.put(id, level);
            }

            return new Enchantments(enchantments, buffer.readBoolean());
        }

        @Override
        public void write(final ByteBuf buffer, final Enchantments value) {
            Type.VAR_INT.writePrimitive(buffer, value.enchantments.size());
            for (final Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
                Type.VAR_INT.writePrimitive(buffer, entry.getIntKey());
                Type.VAR_INT.writePrimitive(buffer, entry.getIntValue());
            }
            buffer.writeBoolean(value.showInTooltip());
        }
    };

    private final Int2IntMap enchantments;
    private final boolean showInTooltip;

    public Enchantments(final Int2IntMap enchantments, final boolean showInTooltip) {
        this.enchantments = enchantments;
        this.showInTooltip = showInTooltip;
    }

    public Enchantments(final boolean showInTooltip) {
        this(new Int2IntOpenHashMap(), showInTooltip);
    }

    public Int2IntMap enchantments() {
        return enchantments;
    }

    public int size() {
        return enchantments.size();
    }

    public boolean showInTooltip() {
        return showInTooltip;
    }

    public void add(final int id, final int level) {
        enchantments.put(id, level);
    }

    public void remove(final int id) {
        enchantments.remove(id);
    }

    public void clear() {
        enchantments.clear();
    }

    public int getLevel(final int id) {
        return enchantments.getOrDefault(id, -1);
    }
}
