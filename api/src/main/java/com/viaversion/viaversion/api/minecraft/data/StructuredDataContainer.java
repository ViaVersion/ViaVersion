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
import com.viaversion.viaversion.api.protocol.Protocol;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;

public final class StructuredDataContainer {

    private final Int2ObjectMap<Optional<StructuredData<?>>> data; // Bless Optionals in a map

    public StructuredDataContainer(final Int2ObjectMap<Optional<StructuredData<?>>> data) {
        this.data = data;
    }

    public StructuredDataContainer() {
        this(new Int2ObjectOpenHashMap<>());
    }

    /**
     * Returns structured data by id. You might want to call {@link #contains(int)} first.
     *
     * @param id  serializer id
     * @param <T> data type
     * @return structured data
     */
    public <T> Optional<StructuredData<T>> get(final int id) {
        final Optional<StructuredData<?>> data = this.data.getOrDefault(id, Optional.empty());
        //noinspection unchecked
        return data.map(value -> (StructuredData<T>) value);
    }

    /**
     * Returns structured data by id. You might want to call {@link #contains(Protocol, StructuredDataKey)} first.
     *
     * @param protocol protocol to retreive the id of the serializer from
     * @param key      serializer id
     * @param <T>      data type
     * @return structured data
     */
    public <T> Optional<StructuredData<T>> get(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKey<T> key) {
        final Optional<StructuredData<?>> data = this.data.getOrDefault(serializerId(protocol, key), Optional.empty());
        //noinspection unchecked
        return data.map(value -> (StructuredData<T>) value);
    }

    public void add(final StructuredData<?> data) {
        this.data.put(data.id(), Optional.of(data));
    }

    public <T> void add(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKey<T> key, final T value) {
        final int id = serializerId(protocol, key);
        if (id != -1) {
            add(new StructuredData<>(key, value, id));
        }
    }

    public <T> void addEmpty(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKey<T> key) {
        final int id = serializerId(protocol, key);
        if (id != -1) {
            this.data.put(id, Optional.empty());
        }
    }

    public void remove(final int id) {
        this.data.remove(id);
    }

    public void removeDefault(final int id) {
        // Empty optional to override the Minecraft default
        this.data.put(id, Optional.empty());
    }

    public boolean contains(final int id) {
        return this.data.containsKey(id);
    }

    public boolean contains(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKey<?> key) {
        return this.data.containsKey(serializerId(protocol, key));
    }

    public StructuredDataContainer copy() {
        return new StructuredDataContainer(new Int2ObjectOpenHashMap<>(data));
    }

    private int serializerId(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKey<?> key) {
        final int id = protocol.getMappingData().getDataComponentSerializerMappings().mappedId(key.identifier());
        if (id == -1) {
            Via.getPlatform().getLogger().severe("Could not find item data serializer for type " + key);
        }
        return id;
    }

    public Int2ObjectMap<Optional<StructuredData<?>>> data() {
        return data;
    }
}
