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
package com.viaversion.viaversion.api.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public record GameProfile(@Nullable String name, @Nullable UUID id, Property[] properties, boolean dynamic) {

    public GameProfile(@Nullable final String name, @Nullable final UUID id, final Property[] properties) {
        this(name, id, properties, true);
    }

    public GameProfile(@Nullable final String name, @Nullable final UUID id) {
        this(name, id, new Property[0]);
    }

    public Map<String, List<Property>> propertiesMap() {
        final Map<String, List<Property>> map = new HashMap<>();
        for (Property property : properties) {
            map.computeIfAbsent(property.name(), k -> new ArrayList<>()).add(property);
        }
        return map;
    }

    public record Property(String name, String value, @Nullable String signature) {

        public Property(final String name, final String value) {
            this(name, value, null);
        }
    }
}
