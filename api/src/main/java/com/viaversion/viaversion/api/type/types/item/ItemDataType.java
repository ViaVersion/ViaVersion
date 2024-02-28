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
package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.item.ItemData;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ItemDataType extends Type<ItemData<?>> {

    private final Int2ObjectMap<Type<?>> types = new Int2ObjectOpenHashMap<>();

    public ItemDataType() {
        super(ItemData.class);
    }

    @Override
    public void write(final ByteBuf buffer, final ItemData<?> object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.id());
        object.write(buffer);
    }

    @Override
    public ItemData<?> read(final ByteBuf buffer) throws Exception {
        final int id = Type.VAR_INT.readPrimitive(buffer);
        final Type<?> type = this.types.get(id);
        if (type != null) {
            return readItemData(buffer, type, id);
        }
        throw new IllegalArgumentException("Unknown item data type id: " + id);
    }

    private <T> ItemData<T> readItemData(final ByteBuf buffer, final Type<T> type, final int id) throws Exception {
        return new ItemData<>(type, type.read(buffer), id);
    }

    public DataFiller filler(final Protocol<?, ?, ?, ?> protocol) {
        return filler(protocol, true);
    }

    public DataFiller filler(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
        return new DataFiller(protocol, useMappedNames);
    }

    public final class DataFiller {

        private final FullMappings mappings;
        private final boolean useMappedNames;

        private DataFiller(final Protocol<?, ?, ?, ?> protocol, final boolean useMappedNames) {
            this.mappings = protocol.getMappingData().getItemDataSerializerMappings();
            this.useMappedNames = useMappedNames;
        }

        public DataFiller reader(final String identifier, final Type<?> reader) {
            types.put(useMappedNames ? mappings.mappedId(identifier) : mappings.id(identifier), reader);
            return this;
        }

        public DataFiller reader(final int id, final Type<?> type) {
            types.put(id, type);
            return this;
        }
    }
}
