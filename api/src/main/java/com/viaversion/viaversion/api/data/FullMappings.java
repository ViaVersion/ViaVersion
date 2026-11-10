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

import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Mappings containing the full string identifier mappings.
 */
public interface FullMappings extends BiMappings {

    /**
     * Returns the unmapped integer id for the given identifier, or -1 if not found.
     *
     * @param identifier unmapped string identifier
     * @return unmapped int id, or -1 if not found
     */
    int id(String identifier);

    /**
     * Returns the mapped integer id for the given mapped identifier, or -1 if not found.
     *
     * @param mappedIdentifier mapped string identifier
     * @return mapped int id, or -1 if not found
     */
    int mappedId(String mappedIdentifier);

    /**
     * Returns the unmapped string identifier for the given mapped id.
     *
     * @param id unmapped id
     * @return unmapped string identifier, or null if out of bounds
     */
    default @Nullable String identifier(int id) {
        final Key key = keyFromId(id);
        return key != null ? key.toString() : null;
    }

    /**
     * Returns the unmapped string identifier for the given mapped identifier.
     *
     * @param mappedIdentifier mapped identifier
     * @return unmapped string identifier, or null if not found
     */
    default @Nullable String identifier(String mappedIdentifier) {
        final Key key = keyFromMappedKey(mappedIdentifier);
        return key != null ? key.toString() : null;
    }

    /**
     * Returns the mapped string identifier for the given mapped id.
     *
     * @param mappedId mapped id
     * @return mapped string identifier, or null if out of bounds
     */
    default @Nullable String mappedIdentifier(int mappedId) {
        final Key key = mappedKeyFromMappedId(mappedId);
        return key != null ? key.toString() : null;
    }

    /**
     * Returns the mapped string identifier for the given unmapped identifier.
     *
     * @param identifier unmapped identifier
     * @return mapped string identifier, or null if not found
     */
    default @Nullable String mappedIdentifier(String identifier) {
        final Key key = mappedKeyFromKey(identifier);
        return key != null ? key.toString() : null;
    }

    /**
     * Returns the unmapped key for the given mapped id.
     *
     * @param id unmapped id
     * @return unmapped key, or null if out of bounds
     */
    @Nullable Key keyFromId(int id);

    /**
     * Returns the unmapped key for the given mapped identifier.
     *
     * @param mappedIdentifier mapped identifier
     * @return unmapped key, or null if not found
     */
    @Nullable Key keyFromMappedKey(String mappedIdentifier);

    /**
     * Returns the mapped key for the given mapped id.
     *
     * @param mappedId mapped id
     * @return mapped key, or null if out of bounds
     */
    @Nullable Key mappedKeyFromMappedId(int mappedId);

    /**
     * Returns the mapped key for the given unmapped identifier.
     *
     * @param identifier unmapped identifier
     * @return mapped key, or null if not found
     */
    @Nullable Key mappedKeyFromKey(String identifier);

    /**
     * Returns whether the int id mappings are identity mappings.
     *
     * @return whether the int id mappings are identity mappings
     * @see #isIdentity()
     */
    boolean isIntIdIdentity();

    @Override
    FullMappings inverse();
}
