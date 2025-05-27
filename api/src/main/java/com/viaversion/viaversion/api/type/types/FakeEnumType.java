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
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import java.util.List;

/**
 * For enum types with hardcoded custom ids.
 *
 * @see EnumType
 */
public final class FakeEnumType extends VarIntType {

    private final Entry[] entries;

    public FakeEnumType(final List<String> initialNames, final Entry... remainingEntries) {
        this.entries = new Entry[initialNames.size() + remainingEntries.length];
        for (int i = 0; i < initialNames.size(); i++) {
            this.entries[i] = Entry.of(i, initialNames.get(i));
        }
        System.arraycopy(remainingEntries, 0, this.entries, initialNames.size(), remainingEntries.length);
    }

    public FakeEnumType(final Entry... entries) {
        this.entries = entries;
    }

    @Override
    public void write(final Ops ops, final Integer value) {
        Entry entry = null;
        for (final Entry e : entries) {
            if (e.id == value) {
                entry = e;
                break;
            }
        }
        Types.STRING.write(ops, entry != null ? entry.name : entries[0].name);
    }

    public Entry[] entries() {
        return entries;
    }

    public record Entry(int id, String name) {

        public static Entry of(final int id, final String name) {
            return new Entry(id, name);
        }
    }
}
