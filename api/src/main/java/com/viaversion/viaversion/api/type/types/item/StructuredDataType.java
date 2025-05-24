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
package com.viaversion.viaversion.api.type.types.item;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredDataType extends Type<StructuredData<?>> implements StructuredDataTypeBase {

    private StructuredDataKey<?>[] types;

    public StructuredDataType() {
        super(StructuredData.class);
    }

    @Override
    public void write(final ByteBuf buffer, final StructuredData<?> object) {
        Types.VAR_INT.writePrimitive(buffer, object.id());
        object.write(buffer);
    }

    @Override
    public StructuredData<?> read(final ByteBuf buffer) {
        Preconditions.checkNotNull(types, "StructuredDataType has not been initialized");
        final int id = Types.VAR_INT.readPrimitive(buffer);
        final StructuredDataKey<?> key = key(id);
        if (key == null) {
            throw new IllegalArgumentException("No data component serializer found for id " + id);
        }
        return readData(buffer, key, id);
    }

    @Override
    public @Nullable StructuredDataKey<?> key(final int id) {
        return id >= 0 && id < types.length ? types[id] : null;
    }

    @Override
    public void write(final Ops ops, final StructuredData<?> data) {
        if (data.isPresent() && ops.context().isSupported(data.key())) {
            writeGeneric(ops, data);
        }
    }

    private <V> void writeGeneric(final Ops ops, final StructuredData<V> data) {
        data.key().type().write(ops, data.value());
    }

    <T> StructuredData<T> readData(final ByteBuf buffer, final StructuredDataKey<T> key, final int id) {
        return StructuredData.of(key, key.type().read(buffer), id);
    }

    public DataFiller filler(final Protocol<?, ?, ?, ?> protocol) {
        final DataFiller filler = new DataFiller(protocol);
        if (protocol.mappedTypes() != null) {
            filler.add(protocol.mappedTypes().structuredDataKeys().keys());
        }
        return filler;
    }

    public final class DataFiller {

        private final FullMappings mappings;

        private DataFiller(final Protocol<?, ?, ?, ?> protocol) {
            this.mappings = protocol.getMappingData().getDataComponentSerializerMappings();
            Preconditions.checkArgument(mappings != null, "No mappings found for protocol %s", protocol.getClass());
            Preconditions.checkArgument(types == null, "StructuredDataType has already been initialized");
            types = new StructuredDataKey[mappings.mappedSize()];
        }

        public DataFiller add(final StructuredDataKey<?> key) {
            final int id = mappings.mappedId(key.identifier());
            Preconditions.checkArgument(id != -1, "No mapped id found for %s", key.identifier());
            Preconditions.checkArgument(types[id] == null, "Data component serializer already exists for id %s", id);
            types[id] = key;
            return this;
        }

        public DataFiller add(final StructuredDataKey<?>... keys) {
            for (final StructuredDataKey<?> key : keys) {
                add(key);
            }
            return this;
        }

        public DataFiller add(final Collection<StructuredDataKey<?>> keys) {
            for (final StructuredDataKey<?> key : keys) {
                add(key);
            }
            return this;
        }
    }
}
