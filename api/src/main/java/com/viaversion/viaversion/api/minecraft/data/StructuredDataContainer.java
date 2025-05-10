/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Unit;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Loosely represents Minecraft's data component patch, but may also be used for an item's full data components.
 * <p>
 * The most commonly used methods will ignore empty data (aka empty overrides that remove item defaults) since those will rarely be needed.
 * These are:
 * <ul>
 *     <li>{@link #get(StructuredDataKey)}</li>
 *     <li>{@link #set(StructuredDataKey, Object)}</li>
 *     <li>{@link #set(StructuredDataKey)}</li>
 *     <li>{@link #getNonEmptyData(StructuredDataKey)}</li>
 *     <li>{@link #hasValue(StructuredDataKey)}</li>
 * </ul>
 *
 * To interact with empty patches specifically, use:
 * <ul>
 *     <li>{@link #setEmpty(StructuredDataKey)}</li>
 *     <li>{@link #hasEmpty(StructuredDataKey)}</li>
 * </ul>
 * Other methods (e.g. {@link #getData(StructuredDataKey)} and {@link #has(StructuredDataKey)}) will handle both empty and non-empty data.
 */
public final class StructuredDataContainer implements Copyable {

    private final Map<StructuredDataKey<?>, StructuredData<?>> data;
    private FullMappings lookup;
    private boolean mappedNames;

    public StructuredDataContainer(final Map<StructuredDataKey<?>, StructuredData<?>> data) {
        this.data = data;
    }

    public StructuredDataContainer(final StructuredData<?>[] dataArray) {
        this(new Reference2ObjectOpenHashMap<>(dataArray.length));
        for (final StructuredData<?> data : dataArray) {
            this.data.put(data.key(), data);
        }
    }

    public StructuredDataContainer() {
        this(new Reference2ObjectOpenHashMap<>(0));
    }

    /**
     * Returns the non-empty value by id if present.
     *
     * @param key serializer id
     * @param <T> data type
     * @return structured data
     * @see #hasEmpty(StructuredDataKey)
     */
    public @Nullable <T> T get(final StructuredDataKey<T> key) {
        final StructuredData<?> data = this.data.get(key);
        if (data == null || data.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return ((StructuredData<T>) data).value();
    }

    /**
     * Returns structured data by id if present, either empty or non-empty.
     *
     * @param key serializer id
     * @param <T> data type
     * @return structured data
     */
    public @Nullable <T> StructuredData<T> getData(final StructuredDataKey<T> key) {
        //noinspection unchecked
        return (StructuredData<T>) this.data.get(key);
    }

    /**
     * Returns non-empty structured data by id if present.
     *
     * @param key serializer id
     * @param <T> data type
     * @return non-empty structured data
     */
    public @Nullable <T> StructuredData<T> getNonEmptyData(final StructuredDataKey<T> key) {
        final StructuredData<?> data = this.data.get(key);
        //noinspection unchecked
        return data != null && data.isPresent() ? (StructuredData<T>) data : null;
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

    public void setEmpty(final StructuredDataKey<?> key) {
        // Empty optional to override the Minecraft default
        this.data.put(key, StructuredData.empty(key, serializerId(key)));
    }

    /**
     * Updates the structured data by id if not empty.
     *
     * @param key         serializer id
     * @param valueMapper function to update existing data
     * @param <T>         data type
     */
    public <T> void replace(final StructuredDataKey<T> key, final Function<T, @Nullable T> valueMapper) {
        final StructuredData<T> data = this.getNonEmptyData(key);
        if (data == null) {
            return;
        }

        final T replacement = valueMapper.apply(data.value());
        if (replacement != null) {
            data.setValue(replacement);
        } else {
            this.data.remove(key);
        }
    }

    public <T> void replaceKey(final StructuredDataKey<T> key, final StructuredDataKey<T> toKey) {
        replace(key, toKey, Function.identity());
    }

    public <T, V> void replace(final StructuredDataKey<T> key, final StructuredDataKey<V> toKey, final Function<T, @Nullable V> valueMapper) {
        final StructuredData<?> data = this.data.remove(key);
        if (data == null) {
            return;
        }

        if (data.isPresent()) {
            //noinspection unchecked
            final T value = (T) data.value();
            final V replacement = valueMapper.apply(value);
            if (replacement != null) {
                set(toKey, replacement);
            }
        } else {
            // Also replace the key for empty data
            setEmpty(toKey);
        }
    }

    /**
     * Removes data by the given key.
     *
     * @param key data key
     * @see #replace(StructuredDataKey, Function)
     * @see #replace(StructuredDataKey, StructuredDataKey, Function)
     * @see #replaceKey(StructuredDataKey, StructuredDataKey)
     */
    public void remove(final StructuredDataKey<?> key) {
        this.data.remove(key);
    }

    /**
     * Removes data by the given keys.
     *
     * @param keys data keys
     * @see #replace(StructuredDataKey, Function)
     * @see #replace(StructuredDataKey, StructuredDataKey, Function)
     * @see #replaceKey(StructuredDataKey, StructuredDataKey)
     */
    public void remove(final Collection<StructuredDataKey<?>> keys) {
        for (final StructuredDataKey<?> key : keys) {
            this.data.remove(key);
        }
    }

    /**
     * Returns whether there is data for the given key, either empty or non-empty.
     *
     * @param key data key
     * @return whether there data for the given key
     * @see #hasEmpty(StructuredDataKey)
     * @see #hasValue(StructuredDataKey)
     */
    public boolean has(final StructuredDataKey<?> key) {
        return this.data.containsKey(key);
    }

    /**
     * Returns whether there is non-empty data for the given key.
     *
     * @param key data key
     * @return whether there is non-empty data for the given key
     */
    public boolean hasValue(final StructuredDataKey<?> key) {
        final StructuredData<?> data = this.data.get(key);
        return data != null && data.isPresent();
    }

    /**
     * Returns whether the structured data has an empty patch/override.
     *
     * @param key serializer id
     * @return whether the structured data has an empty patch/override
     */
    public boolean hasEmpty(final StructuredDataKey<?> key) {
        final StructuredData<?> data = this.data.get(key);
        return data != null && data.isEmpty();
    }

    /**
     * Sets the lookup for serializer ids. Required to call most of the other methods.
     *
     * @param protocol    protocol to retreive the id of the serializer from
     * @param mappedNames if the names are mapped (true if structures from the mapped version are added, false for the unmapped version)
     */
    public void setIdLookup(final Protocol<?, ?, ?, ?> protocol, final boolean mappedNames) {
        this.lookup = protocol.getMappingData().getDataComponentSerializerMappings();
        Preconditions.checkNotNull(this.lookup, "Data component serializer mappings are null");
        this.mappedNames = mappedNames;
    }

    public void updateIds(final Protocol<?, ?, ?, ?> protocol, final Int2IntFunction rewriter) {
        for (final StructuredData<?> data : data.values()) {
            final int mappedId = rewriter.applyAsInt(data.id());
            if (mappedId == -1) {
                continue;
            }

            data.setId(mappedId);
        }
    }

    @Override
    public StructuredDataContainer copy() {
        final Reference2ObjectOpenHashMap<StructuredDataKey<?>, StructuredData<?>> map = new Reference2ObjectOpenHashMap<>();
        for (final StructuredData<?> value : data.values()) {
            map.put(value.key(), value.copy());
        }
        final StructuredDataContainer copy = new StructuredDataContainer(map);
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

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public String toString() {
        return "StructuredDataContainer{" +
            "data=" + data +
            ", lookup=" + lookup +
            ", mappedNames=" + mappedNames +
            '}';
    }
}
