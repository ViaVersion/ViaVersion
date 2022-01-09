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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class ParticleMappings {
    private final Object2IntMap<String> stringToId;
    private final Object2IntMap<String> mappedStringToId;
    private final Mappings mappings;
    private final IntList itemParticleIds = new IntArrayList(2);
    private final IntList blockParticleIds = new IntArrayList(4);

    public ParticleMappings(JsonArray oldMappings, JsonArray newMappings, Mappings mappings) {
        this.mappings = mappings;
        stringToId = MappingDataLoader.arrayToMap(oldMappings);
        mappedStringToId = MappingDataLoader.arrayToMap(newMappings);
        stringToId.defaultReturnValue(-1);
        mappedStringToId.defaultReturnValue(-1);
        addBlockParticle("block");
        addBlockParticle("falling_dust");
        addBlockParticle("block_marker");
        addItemParticle("item");
    }

    /**
     * Returns the unmapped integer id for the given identifier, or -1 if not found.
     *
     * @param identifier unmapped string identifier
     * @return unmapped int id, or -1 if not found
     */
    public int id(String identifier) {
        return stringToId.getInt(identifier);
    }

    /**
     * Returns the mapped integer id for the given mapped identifier, or -1 if not found.
     *
     * @param mappedIdentifier mapped string identifier
     * @return mapped int id, or -1 if not found
     */
    public int mappedId(String mappedIdentifier) {
        return mappedStringToId.getInt(mappedIdentifier);
    }

    public Mappings getMappings() {
        return mappings;
    }

    public boolean addItemParticle(final String identifier) {
        final int id = id(identifier);
        return id != -1 && itemParticleIds.add(id);
    }

    public boolean addBlockParticle(final String identifier) {
        final int id = id(identifier);
        return id != -1 && blockParticleIds.add(id);
    }

    public boolean isBlockParticle(final int id) {
        return blockParticleIds.contains(id);
    }

    public boolean isItemParticle(final int id) {
        return itemParticleIds.contains(id);
    }

    @Deprecated/*(forRemoval = true)*/
    public int getBlockId() {
        return id("block");
    }

    @Deprecated/*(forRemoval = true)*/
    public int getFallingDustId() {
        return id("falling_dust");
    }

    @Deprecated/*(forRemoval = true)*/
    public int getItemId() {
        return id("item");
    }
}
