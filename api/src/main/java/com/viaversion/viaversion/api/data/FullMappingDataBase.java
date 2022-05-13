/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

import com.google.gson.JsonArray;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FullMappingDataBase implements FullMappingData {
    private final Object2IntMap<String> stringToId;
    private final Object2IntMap<String> mappedStringToId;
    private final String[] idToString;
    private final String[] mappedIdToString;
    private final Mappings mappings;

    public FullMappingDataBase(final JsonArray oldMappings, final JsonArray newMappings, final Mappings mappings) {
        this.mappings = mappings;
        stringToId = MappingDataLoader.arrayToMap(oldMappings);
        mappedStringToId = MappingDataLoader.arrayToMap(newMappings);
        stringToId.defaultReturnValue(-1);
        mappedStringToId.defaultReturnValue(-1);

        idToString = new String[oldMappings.size()];
        for (int i = 0; i < oldMappings.size(); i++) {
            idToString[i] = oldMappings.get(i).getAsString();
        }

        mappedIdToString = new String[newMappings.size()];
        for (int i = 0; i < newMappings.size(); i++) {
            mappedIdToString[i] = newMappings.get(i).getAsString();
        }
    }

    @Override
    public Mappings mappings() {
        return mappings;
    }

    @Override
    public int id(final String identifier) {
        return stringToId.getInt(identifier);
    }

    @Override
    public int mappedId(final String mappedIdentifier) {
        return mappedStringToId.getInt(mappedIdentifier);
    }

    @Override
    public String identifier(final int id) {
        return idToString[id];
    }

    @Override
    public String mappedIdentifier(final int mappedId) {
        return mappedIdToString[mappedId];
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
}
