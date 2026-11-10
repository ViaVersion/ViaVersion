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

import com.viaversion.viaversion.api.data.MappingDataLoader.IdentifiersPair;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Full mappings for versions where required identifiers hadn't yet followed the resource location format (i.e., pre-1.9.4 sounds).
 */
public class LegacyFullStringMappingsBase extends FullMappingsBase {
    private final String[] idToString;
    private final String[] mappedIdToString;

    public LegacyFullStringMappingsBase(final IdentifiersPair identifiersPair, final Mappings mappings) {
        super(toInverseMap(identifiersPair.unmapped()), toInverseMap(identifiersPair.mapped()), new Key[0], new Key[0], mappings);
        this.idToString = identifiersPair.unmapped().toArray(new String[0]);
        this.mappedIdToString = identifiersPair.mapped().toArray(new String[0]);
    }

    private LegacyFullStringMappingsBase(final Object2IntMap<String> stringToId, final Object2IntMap<String> mappedStringToId, final String[] idToKey, final String[] mappedIdToString, final Mappings mappings) {
        super(stringToId, mappedStringToId, new Key[0], new Key[0], mappings);
        this.idToString = idToKey;
        this.mappedIdToString = mappedIdToString;
    }

    @Override
    public String identifier(final int id) {
        if (id < 0 || id >= idToString.length) {
            return null;
        }
        return Key.namespaced(idToString[id]);
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
        return Key.namespaced(mappedIdToString[mappedId]);
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
    public @Nullable Key keyFromId(final int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Key keyFromMappedKey(final String mappedIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Key mappedKeyFromMappedId(final int mappedId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Key mappedKeyFromKey(final String identifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FullMappings inverse() {
        return new LegacyFullStringMappingsBase(mappedStringToId, stringToId, mappedIdToString, idToString, mappings.inverse());
    }
}
