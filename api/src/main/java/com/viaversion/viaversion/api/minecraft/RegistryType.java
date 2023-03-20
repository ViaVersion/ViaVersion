/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum RegistryType {

    BLOCK("block"),
    ITEM("item"),
    FLUID("fluid"),
    ENTITY("entity_type"),
    GAME_EVENT("game_event");

    private static final Map<String, RegistryType> MAP = new HashMap<>();
    private static final RegistryType[] VALUES = values();

    static {
        for (RegistryType type : getValues()) {
            MAP.put(type.resourceLocation, type);
        }
    }

    public static RegistryType[] getValues() {
        return VALUES;
    }

    public static @Nullable RegistryType getByKey(String resourceKey) {
        return MAP.get(resourceKey);
    }

    private final String resourceLocation;

    RegistryType(final String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    @Deprecated/*(forRemoval = true)*/
    public String getResourceLocation() {
        return resourceLocation;
    }

    public String resourceLocation() {
        return resourceLocation;
    }
}
