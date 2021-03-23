/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Mappings {
    protected final short[] oldToNew;

    public Mappings(short[] oldToNew) {
        this.oldToNew = oldToNew;
    }

    /**
     * Maps old identifiers to the new ones.
     * If an old value cannot be found in the new mappings, the diffmapping will be checked for the given entry.
     *
     * @param size        set size of the underlying short array
     * @param oldMapping  mappings to map from
     * @param newMapping  mappings to map to
     * @param diffMapping extra mappings that will be used/scanned when an entry cannot be found
     */
    public Mappings(int size, JsonObject oldMapping, JsonObject newMapping, @Nullable JsonObject diffMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping, diffMapping);
    }

    public Mappings(JsonObject oldMapping, JsonObject newMapping, @Nullable JsonObject diffMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping, diffMapping);
    }

    /**
     * Maps old identifiers to the new ones.
     *
     * @param size       set size of the underlying short array
     * @param oldMapping mappings to map from
     * @param newMapping mappings to map to
     */
    public Mappings(int size, JsonObject oldMapping, JsonObject newMapping) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping);
    }

    public Mappings(JsonObject oldMapping, JsonObject newMapping) {
        this(oldMapping.entrySet().size(), oldMapping, newMapping);
    }

    /**
     * Maps old identifiers to the new ones.
     *
     * @param size          set size of the underlying short array
     * @param oldMapping    mappings to map from
     * @param newMapping    mappings to map to
     * @param diffMapping   extra mappings that will be used/scanned when an entry cannot be found
     * @param warnOnMissing should "No key for x" be printed if there is no matching identifier
     */
    public Mappings(int size, JsonArray oldMapping, JsonArray newMapping, JsonObject diffMapping, boolean warnOnMissing) {
        oldToNew = new short[size];
        Arrays.fill(oldToNew, (short) -1);
        MappingDataLoader.mapIdentifiers(oldToNew, oldMapping, newMapping, diffMapping, warnOnMissing);
    }

    public Mappings(int size, JsonArray oldMapping, JsonArray newMapping, boolean warnOnMissing) {
        this(size, oldMapping, newMapping, null, warnOnMissing);
    }

    public Mappings(JsonArray oldMapping, JsonArray newMapping, boolean warnOnMissing) {
        this(oldMapping.size(), oldMapping, newMapping, warnOnMissing);
    }

    public Mappings(int size, JsonArray oldMapping, JsonArray newMapping) {
        this(size, oldMapping, newMapping, true);
    }

    public Mappings(JsonArray oldMapping, JsonArray newMapping, JsonObject diffMapping) {
        this(oldMapping.size(), oldMapping, newMapping, diffMapping, true);
    }

    public Mappings(JsonArray oldMapping, JsonArray newMapping) {
        this(oldMapping.size(), oldMapping, newMapping, true);
    }

    public int getNewId(int old) {
        return old >= 0 && old < oldToNew.length ? oldToNew[old] : -1;
    }

    public short[] getOldToNew() {
        return oldToNew;
    }
}
