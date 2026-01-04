/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class KeyMappings {

    private final Object2IntMap<String> keyToId;
    private final String[] keys;

    public KeyMappings(final String... keys) {
        this.keys = keys;
        keyToId = new Object2IntOpenHashMap<>(keys.length);
        keyToId.defaultReturnValue(-1);
        for (int i = 0; i < keys.length; i++) {
            keyToId.put(keys[i], i);
        }
    }

    public KeyMappings(final Collection<String> keys) {
        this(keys.toArray(new String[0]));
    }

    public KeyMappings(final ListTag<StringTag> keys) {
        this(keys.getValue().stream().map(StringTag::getValue).toArray(String[]::new));
    }

    public @Nullable String idToKey(final int id) {
        if (id < 0 || id >= keys.length) {
            return null;
        }
        return keys[id];
    }

    public int keyToId(final String identifier) {
        return keyToId.getInt(Key.stripMinecraftNamespace(identifier));
    }

    public String[] keys() {
        return keys;
    }

    public int size() {
        return keys.length;
    }
}
