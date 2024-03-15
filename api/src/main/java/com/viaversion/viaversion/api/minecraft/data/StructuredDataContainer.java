/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.data;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StructuredDataContainer {

    private final Map<StructuredDataKey<?>, StructuredData<?>> data;
    private FullMappings lookup;
    private boolean mappedNames;

    public StructuredDataContainer(final Map<StructuredDataKey<?>, StructuredData<?>> data) {
        this.data = data;
    }

    public StructuredDataContainer() {
        this(new Reference2ObjectOpenHashMap<>());
    }

    /**
     * Returns structured data by id if present.
     *
     * @param key serializer id
     * @param <T> data type
     * @return structured data
     */
    public @Nullable <T> StructuredData<T> get(final StructuredDataKey<T> key) {
        //noinspection unchecked
        return (StructuredData<T>) this.data.get(key);
    }

    /**
     * Returns structured data by id if not empty.
     *
     * @param key serializer id
     * @param <T> data type
     * @return structured data if not empty
     */
    public @Nullable <T> StructuredData<T> getNonEmpty(final StructuredDataKey<T> key) {
        //noinspection unchecked
        final StructuredData<T> data = (StructuredData<T>) this.data.get(key);
        return data != null && data.isPresent() ? data : null;
    }

    public <T> void set(final StructuredDataKey<T> key, final T value) {
        final int id = serializerId(key);
        if (id != -1) {
            this.data.put(key, StructuredData.of(key, value, id));
        }
    }

    public void set(final StructuredDataKey<Unit> key) {
        this.set(key, Unit.INSTANCE);
    }

    public void addEmpty(final StructuredDataKey<?> key) {
        // Empty optional to override the Minecraft default
        this.data.put(key, StructuredData.empty(key, serializerId(key)));
    }

    /**
     * Removes and returns structured data by the given key.
     *
     * @param key serializer key
     * @param <T> data type
     * @return removed structured data
     */
    public @Nullable <T> StructuredData<T> remove(final StructuredDataKey<T> key) {
        final StructuredData<?> data = this.data.remove(key);
        //noinspection unchecked
        return data != null ? (StructuredData<T>) data : null;
    }

    public boolean contains(final StructuredDataKey<?> key) {
        return this.data.containsKey(key);
    }

    /**
     * Sets the lookup for serializer ids. Required to call most of the other methods.
     *
     * @param protocol    protocol to retreive the id of the serializer from
     * @param mappedNames if the names are mapped (true if structures from the mapped version are added, false for the unmapped version)
     */
    public void setIdLookup(final Protocol<?, ?, ?, ?> protocol, final boolean mappedNames) {
        this.lookup = protocol.getMappingData().getDataComponentSerializerMappings();
        this.mappedNames = mappedNames;
    }

    public StructuredDataContainer copy() {
        final StructuredDataContainer copy = new StructuredDataContainer(new Reference2ObjectOpenHashMap<>(data));
        copy.lookup = this.lookup;
        return copy;
    }

    private int serializerId(final StructuredDataKey<?> key) {
        final int id = mappedNames ? lookup.mappedId(key.identifier()) : lookup.id(key.identifier());
        if (id == -1) {
            Via.getPlatform().getLogger().severe("Could not find item data serializer for type " + key);
        }
        return id;
    }

    public Map<StructuredDataKey<?>, StructuredData<?>> data() {
        return data;
    }

    @Override
    public String toString() {
        return "StructuredDataContainer{" +
            "data=" + data +
            '}';
    }
}
