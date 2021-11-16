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
package com.viaversion.viaversion.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface Mappings {

    /**
     * Returns the mapped id from the given id, or -1 if invalid/out of bounds.
     *
     * @param id unmapped id
     * @return mapped id, or -1 if invalid/out of bounds
     */
    int getNewId(int id);

    /**
     * Manually maps a specific id.
     *
     * @param id    unmapped id
     * @param newId mapped id
     * @throws IndexOutOfBoundsException if the unmapped id is invalid
     */
    void setNewId(int id, int newId);

    /**
     * Returns amount of unmapped entries, being the size of the mapping.
     *
     * @return amount of unmapped entries
     */
    int size();

    /**
     * Returns the amount of new ids total, even if it does not have a direct mapping.
     * Returns -1 if unknown.
     *
     * @return amount of new ids, or -1 if unknown
     */
    int mappedSize();

    static <T extends Mappings> Builder<T> builder(final MappingsSupplier<T> supplier) {
        return new Builder(supplier);
    }

    @FunctionalInterface
    interface MappingsSupplier<T extends Mappings> {

        T supply(int[] mappings, int mappedIds);
    }

    class Builder<T extends Mappings> {

        private final MappingsSupplier<T> supplier;
        private JsonElement unmapped;
        private JsonElement mapped;
        private JsonObject diffMappings;
        private int mappedSize = -1;
        private int size = -1;
        private boolean warnOnMissing = true;

        protected Builder(final MappingsSupplier<T> supplier) {
            this.supplier = supplier;
        }

        /**
         * Sets a custom entry size different to the size of the unmapped collection.
         *
         * @param size custom entry size
         * @return self
         */
        public Builder<T> customEntrySize(final int size) {
            this.size = size;
            return this;
        }

        /**
         * Sets a custom entry mapped ids count different to the size of the mapped collection.
         *
         * @param size custom mapped id count
         * @return self
         */
        public Builder<T> customMappedSize(final int size) {
            this.mappedSize = size;
            return this;
        }

        /**
         * Sets whether warnings should be logged for missing mapped ids.
         *
         * @param warnOnMissing whether warnings should be logged for missing mapped ids
         * @return self
         */
        public Builder<T> warnOnMissing(final boolean warnOnMissing) {
            this.warnOnMissing = warnOnMissing;
            return this;
        }

        public Builder<T> unmapped(final JsonArray unmappedArray) {
            this.unmapped = unmappedArray;
            return this;
        }

        public Builder<T> unmapped(final JsonObject unmappedObject) {
            this.unmapped = unmappedObject;
            return this;
        }

        public Builder<T> mapped(final JsonArray mappedArray) {
            this.mapped = mappedArray;
            return this;
        }

        public Builder<T> mapped(final JsonObject mappedObject) {
            this.mapped = mappedObject;
            return this;
        }

        public Builder<T> diffMappings(final JsonObject diffMappings) {
            this.diffMappings = diffMappings;
            return this;
        }

        public T build() {
            final int size = this.size != -1 ? this.size : size(unmapped);
            final int mappedSize = this.mappedSize != -1 ? this.mappedSize : size(mapped);
            final int[] mappings = new int[size];

            // Do conversion if one is an array and the other an object, otherwise directly map
            if (unmapped.isJsonArray()) {
                if (mapped.isJsonObject()) {
                    MappingDataLoader.mapIdentifiers(mappings, toJsonObject(unmapped.getAsJsonArray()), mapped.getAsJsonObject(), diffMappings, warnOnMissing);
                } else {
                    MappingDataLoader.mapIdentifiers(mappings, unmapped.getAsJsonArray(), mapped.getAsJsonArray(), diffMappings, warnOnMissing);
                }
            } else if (mapped.isJsonArray()) {
                MappingDataLoader.mapIdentifiers(mappings, unmapped.getAsJsonObject(), toJsonObject(mapped.getAsJsonArray()), diffMappings, warnOnMissing);
            } else {
                MappingDataLoader.mapIdentifiers(mappings, unmapped.getAsJsonObject(), mapped.getAsJsonObject(), diffMappings, warnOnMissing);
            }

            return supplier.supply(mappings, mappedSize);
        }

        protected int size(final JsonElement element) {
            return element.isJsonObject() ? element.getAsJsonObject().size() : element.getAsJsonArray().size();
        }

        protected JsonObject toJsonObject(final JsonArray array) {
            final JsonObject object = new JsonObject();
            for (int i = 0; i < array.size(); i++) {
                final JsonElement element = array.get(i);
                object.add(Integer.toString(i), element);
            }
            return object;
        }
    }
}
