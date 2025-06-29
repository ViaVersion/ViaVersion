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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import java.util.Map;
import java.util.Set;

/**
 * Only strictly needed for hashing purposes for now.
 */
public class TextComponentTagType extends TagType {

    private static final Set<String> BOOLEAN_KEYS = Set.of("bold", "italic", "underlined", "strikethrough", "obfuscated", "interpret");

    @Override
    public void write(final Ops ops, final Tag value) {
        if (value instanceof final CompoundTag compoundTag) {
            ops.writeMap(map -> {
                for (final Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    write(map, entry.getKey(), entry.getValue());
                }
            });
        } else {
            super.write(ops, value);
        }
    }

    private void write(final Ops.MapSerializer map, final String key, final Tag value) {
        // Better than fully parsing and re-serializing the component
        if (value instanceof final ByteTag byteTag && BOOLEAN_KEYS.contains(key)) {
            map.write(key, Types.BOOLEAN, byteTag.asBoolean());
        } else {
            map.write(key, this, value);
        }
    }
}
