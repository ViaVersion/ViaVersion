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
package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.IdHolder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class DynamicType<T extends IdHolder> extends Type<T> {

    protected final Int2ObjectMap<DataReader<T>> readers;

    protected DynamicType(final Int2ObjectMap<DataReader<T>> readers, final Class<T> outputClass) {
        super(outputClass.getSimpleName(), outputClass);
        this.readers = readers;
    }

    protected DynamicType(final Class<T> outputClass) {
        this(new Int2ObjectOpenHashMap<>(), outputClass);
    }

    public DataFiller filler(final Protocol<?, ?, ?, ?> protocol) {
        return filler(protocol, true);
    }

    public DataFiller filler(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
        return new DataFiller(protocol, useMappedNames);
    }

    protected void readData(final ByteBuf buffer, final T value) throws Exception {
        final DataReader<T> reader = readers.get(value.id());
        if (reader != null) {
            reader.read(buffer, value);
        }
    }

    public RawDataFiller rawFiller() {
        return new RawDataFiller();
    }

    public final class DataFiller {

        private final FullMappings mappings;
        private final boolean useMappedNames;

        private DataFiller(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
            this.mappings = mappings(protocol);
            Preconditions.checkNotNull(mappings, "Mappings for %s are null", protocol.getClass());
            this.useMappedNames = useMappedNames;
        }

        public DataFiller reader(final String identifier, final DataReader<T> reader) {
            readers.put(useMappedNames ? mappings.mappedId(identifier) : mappings.id(identifier), reader);
            return this;
        }
    }

    public final class RawDataFiller {

        public RawDataFiller reader(final int id, final DataReader<T> reader) {
            readers.put(id, reader);
            return this;
        }
    }

    protected abstract FullMappings mappings(Protocol<?, ?, ?, ?> protocol);

    @FunctionalInterface
    public interface DataReader<T> {

        /**
         * Reads a value from the buffer and adds it to the data.
         *
         * @param buf   buffer
         * @param value value
         * @throws Exception if an error occurs during buffer reading
         */
        void read(ByteBuf buf, T value) throws Exception;
    }
}
