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
package com.viaversion.viaversion.api.data;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.MappingDataLoader.IdentifiersPair;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FullMappingsBase implements FullMappings {
    private static final String[] EMPTY_ARRAY = new String[0];
    private final Object2IntMap<String> stringToId;
    private final Object2IntMap<String> mappedStringToId;
    private final String[] idToString;
    private final String[] mappedIdToString;
    private final Mappings mappings;

    public FullMappingsBase(final IdentifiersPair identifiersPair, final Mappings mappings) {
        Preconditions.checkNotNull(mappings, "Mappings cannot be null");
        this.mappings = mappings;
        this.stringToId = toInverseMap(identifiersPair.unmapped());
        this.idToString = identifiersPair.unmapped().toArray(EMPTY_ARRAY);
        this.mappedStringToId = toInverseMap(identifiersPair.mapped());
        this.mappedIdToString = identifiersPair.mapped().toArray(EMPTY_ARRAY);
    }

    private FullMappingsBase(final Object2IntMap<String> stringToId, final Object2IntMap<String> mappedStringToId, final String[] idToString, final String[] mappedIdToString, final Mappings mappings) {
        this.stringToId = stringToId;
        this.mappedStringToId = mappedStringToId;
        this.idToString = idToString;
        this.mappedIdToString = mappedIdToString;
        this.mappings = mappings;
    }

    public static FullMappings of(final IdentifiersPair identifiersPair, final Mappings mappings) {
        return mappings.isIdentity() && identifiersPair.identity() ? new FullIdentityMappings(identifiersPair, mappings) : new FullMappingsBase(identifiersPair, mappings);
    }

    static Object2IntMap<String> toInverseMap(final List<String> list) {
        final Object2IntMap<String> map = new Object2IntOpenHashMap<>(list.size());
        map.defaultReturnValue(-1);
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
        return map;
    }

    @Override
    public int id(final String identifier) {
        return stringToId.getInt(Key.stripMinecraftNamespace(identifier));
    }

    @Override
    public int mappedId(final String mappedIdentifier) {
        return mappedStringToId.getInt(Key.stripMinecraftNamespace(mappedIdentifier));
    }

    @Override
    public String identifier(final int id) {
        if (id < 0 || id >= idToString.length) {
            return null;
        }

        final String identifier = idToString[id];
        return Key.namespaced(identifier);
    }

    @Override
    public @Nullable String identifier(final String mappedIdentifier) {
        final int mappedId = mappedId(mappedIdentifier);
        if (mappedId == -1) {
            return null;
        }

        final int id = mappings.inverse().getNewId(mappedId);
        return id != -1 ? identifier(id) : null;
    }

    @Override
    public @Nullable String mappedIdentifier(final int mappedId) {
        if (mappedId < 0 || mappedId >= mappedIdToString.length) {
            return null;
        }

        final String identifier = mappedIdToString[mappedId];
        return Key.namespaced(identifier);
    }

    @Override
    public @Nullable String mappedIdentifier(final String identifier) {
        final int id = id(identifier);
        if (id == -1) {
            return null;
        }

        final int mappedId = mappings.getNewId(id);
        return mappedId != -1 ? mappedIdentifier(mappedId) : null;
    }

    @Override
    public int getNewId(int id) {
        return mappings.getNewId(id);
    }

    @Override
    public void setNewId(int id, int mappedId) {
        mappings.setNewId(id, mappedId);
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public int mappedSize() {
        return mappings.mappedSize();
    }

    @Override
    public boolean isIntIdIdentity() {
        return mappings.isIdentity();
    }

    @Override
    public FullMappings inverse() {
        return new FullMappingsBase(mappedStringToId, stringToId, mappedIdToString, idToString, mappings.inverse());
    }
}
