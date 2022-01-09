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
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

public class IntArrayMappings implements Mappings {
    private final int[] oldToNew;
    private final int mappedIds;

    protected IntArrayMappings(final int[] oldToNew, final int mappedIds) {
        this.oldToNew = oldToNew;
        this.mappedIds = mappedIds;
    }

    public static IntArrayMappings of(final int[] oldToNew, final int mappedIds) {
        return new IntArrayMappings(oldToNew, mappedIds);
    }

    public static Builder<IntArrayMappings> builder() {
        return Mappings.builder(IntArrayMappings::new);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int[] oldToNew) {
        this(oldToNew, -1);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int size, JsonObject oldMapping, JsonObject newMapping, @Nullable JsonObject diffMapping) {
        oldToNew = new int[size];
        Arrays.fill(oldToNew, -1);
        this.mappedIds = newMapping.size();
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping, diffMapping);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(JsonObject oldMapping, JsonObject newMapping, @Nullable JsonObject diffMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping, diffMapping);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int size, JsonObject oldMapping, JsonObject newMapping) {
        oldToNew = new int[size];
        Arrays.fill(oldToNew, -1);
        mappedIds = -1;
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(JsonObject oldMapping, JsonObject newMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int size, JsonArray oldMapping, JsonArray newMapping, JsonObject diffMapping, boolean warnOnMissing) {
        oldToNew = new int[size];
        Arrays.fill(oldToNew, -1);
        mappedIds = -1;
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping, diffMapping, warnOnMissing);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int size, JsonArray oldMapping, JsonArray newMapping, boolean warnOnMissing) {
        this(size, oldMapping, newMapping, null, warnOnMissing);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(JsonArray oldMapping, JsonArray newMapping, boolean warnOnMissing) {
        this(oldMapping.size(), oldMapping, newMapping, warnOnMissing);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(int size, JsonArray oldMapping, JsonArray newMapping) {
        this(size, oldMapping, newMapping, true);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(JsonArray oldMapping, JsonArray newMapping, JsonObject diffMapping) {
        this(oldMapping.size(), oldMapping, newMapping, diffMapping, true);
    }

    @Deprecated/*(forRemoval = true)*/
    public IntArrayMappings(JsonArray oldMapping, JsonArray newMapping) {
        this(oldMapping.size(), oldMapping, newMapping, true);
    }

    @Override
    public int getNewId(int id) {
        return id >= 0 && id < oldToNew.length ? oldToNew[id] : -1;
    }

    @Override
    public void setNewId(int id, int newId) {
        oldToNew[id] = newId;
    }

    @Override
    public int size() {
        return oldToNew.length;
    }

    @Override
    public int mappedSize() {
        return mappedIds;
    }

    public int[] getOldToNew() {
        return oldToNew;
    }
}
