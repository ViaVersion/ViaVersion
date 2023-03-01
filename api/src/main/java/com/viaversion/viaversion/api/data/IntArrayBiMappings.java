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
package com.viaversion.viaversion.api.data;

import java.util.Arrays;

public class IntArrayBiMappings implements BiMappings {

    private final Mappings mappings;
    private final IntArrayBiMappings inverse;

    protected IntArrayBiMappings(final Mappings mappings, final Mappings inverseMappings) {
        this.mappings = mappings;
        this.inverse = new IntArrayBiMappings(inverseMappings, this);
    }

    private IntArrayBiMappings(final Mappings mappings, final IntArrayBiMappings inverse) {
        this.mappings = mappings;
        this.inverse = inverse;
    }

    public static Mappings.Builder<IntArrayBiMappings> builder() {
        return new Builder(IntArrayMappings::new);
    }

    @Override
    public BiMappings inverse() {
        return this.inverse;
    }

    @Override
    public int getNewId(final int id) {
        return mappings.getNewId(id);
    }

    @Override
    public void setNewId(final int id, final int newId) {
        mappings.setNewId(id, newId);
        inverse.mappings.setNewId(newId, id);
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public int mappedSize() {
        return mappings.mappedSize();
    }

    public static final class Builder extends Mappings.Builder<IntArrayBiMappings> {

        private final MappingsSupplier<?> supplier;

        private Builder(final MappingsSupplier<?> supplier) {
            super(null);
            this.supplier = supplier;
        }

        @Override
        public IntArrayBiMappings build() {
            final int size = this.size != -1 ? this.size : size(unmapped);
            final int mappedSize = this.mappedSize != -1 ? this.mappedSize : size(mapped);
            final int[] mappingsArray = new int[size];
            final int[] inverseMappingsArray = new int[mappedSize];
            Arrays.fill(mappingsArray, -1);
            Arrays.fill(inverseMappingsArray, -1);

            final Mappings mappings = supplier.supply(mappingsArray, mappedSize);
            final Mappings inverseMappings = supplier.supply(inverseMappingsArray, size);
            if (unmapped.isJsonArray() && mapped.isJsonArray()) {
                MappingDataLoader.mapIdentifiers(mappings, inverseMappings, unmapped.getAsJsonArray(), mapped.getAsJsonArray(), diffMappings, true);
            } else {
                throw new UnsupportedOperationException();
            }

            return new IntArrayBiMappings(mappings, inverseMappings);
        }
    }
}
