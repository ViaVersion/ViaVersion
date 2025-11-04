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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public record Enchantments(Int2IntMap enchantments, boolean showInTooltip) implements Copyable {

    public Enchantments(final Int2IntMap enchantments) {
        this(enchantments, true);
    }

    public static final Type<Enchantments> TYPE1_20_5 = new Type<>(Enchantments.class) {
        @Override
        public Enchantments read(final ByteBuf buffer) {
            final Int2IntMap enchantments = new Int2IntOpenHashMap();
            final int size = Types.VAR_INT.readPrimitive(buffer);
            for (int i = 0; i < size; i++) {
                final int id = Types.VAR_INT.readPrimitive(buffer);
                final int level = Types.VAR_INT.readPrimitive(buffer);
                enchantments.put(id, level);
            }

            return new Enchantments(enchantments, buffer.readBoolean());
        }

        @Override
        public void write(final ByteBuf buffer, final Enchantments value) {
            Types.VAR_INT.writePrimitive(buffer, value.enchantments.size());
            for (final Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
                Types.VAR_INT.writePrimitive(buffer, entry.getIntKey());
                Types.VAR_INT.writePrimitive(buffer, entry.getIntValue());
            }
            buffer.writeBoolean(value.showInTooltip());
        }
    };
    public static final Type<Enchantments> TYPE1_21_5 = new Type<>(Enchantments.class) {
        @Override
        public Enchantments read(final ByteBuf buffer) {
            final Int2IntMap enchantments = new Int2IntOpenHashMap();
            final int size = Types.VAR_INT.readPrimitive(buffer);
            for (int i = 0; i < size; i++) {
                final int id = Types.VAR_INT.readPrimitive(buffer);
                final int level = Types.VAR_INT.readPrimitive(buffer);
                enchantments.put(id, level);
            }

            return new Enchantments(enchantments);
        }

        @Override
        public void write(final ByteBuf buffer, final Enchantments value) {
            Types.VAR_INT.writePrimitive(buffer, value.enchantments.size());
            for (final Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
                Types.VAR_INT.writePrimitive(buffer, entry.getIntKey());
                Types.VAR_INT.writePrimitive(buffer, entry.getIntValue());
            }
        }

        @Override
        public void write(final Ops ops, final Enchantments value) {
            ops.writeMap(map -> {
                for (final Int2IntMap.Entry entry : value.enchantments.int2IntEntrySet()) {
                    final Key key = ops.context().registryAccess().registryKey("enchantment", entry.getIntKey());
                    map.write(Types.IDENTIFIER, key, Types.VAR_INT, entry.getIntValue());
                }
            });
        }
    };

    public Enchantments(final boolean showInTooltip) {
        this(new Int2IntOpenHashMap(), showInTooltip);
    }

    public int size() {
        return enchantments.size();
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

    @Override
    public Enchantments copy() {
        return new Enchantments(new Int2IntOpenHashMap(enchantments), showInTooltip);
    }
}
