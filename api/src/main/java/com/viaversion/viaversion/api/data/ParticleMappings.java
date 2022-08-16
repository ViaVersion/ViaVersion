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

public class ParticleMappings extends FullMappingsBase {
    private final IntList itemParticleIds = new IntArrayList(2);
    private final IntList blockParticleIds = new IntArrayList(4);

    public ParticleMappings(JsonArray oldMappings, JsonArray newMappings, Mappings mappings) {
        super(oldMappings, newMappings, mappings);
        addBlockParticle("block");
        addBlockParticle("falling_dust");
        addBlockParticle("block_marker");
        addItemParticle("item");
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
