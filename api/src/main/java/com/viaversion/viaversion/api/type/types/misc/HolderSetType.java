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
package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class HolderSetType extends Type<HolderSet> {

    private final MappingDataBase.MappingType mappingType;

    public HolderSetType() {
        this(null);
    }

    public HolderSetType(final MappingDataBase.MappingType mappingType) {
        super(HolderSet.class);
        this.mappingType = mappingType;
    }

    @Override
    public HolderSet read(final ByteBuf buffer) {
        final int size = Types.VAR_INT.readPrimitive(buffer) - 1;
        if (size == -1) {
            final String tag = Types.STRING.read(buffer);
            return HolderSet.of(tag);
        }

        final int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = Types.VAR_INT.readPrimitive(buffer);
        }
        return HolderSet.of(values);
    }

    @Override
    public void write(final ByteBuf buffer, final HolderSet object) {
        if (object.hasTagKey()) {
            Types.VAR_INT.writePrimitive(buffer, 0);
            Types.STRING.write(buffer, object.tagKey());
        } else {
            final int[] values = object.ids();
            Types.VAR_INT.writePrimitive(buffer, values.length + 1);
            for (final int value : values) {
                Types.VAR_INT.writePrimitive(buffer, value);
            }
        }
    }

    @Override
    public void write(final Ops ops, final HolderSet value) {
        if (value.hasTagKey()) {
            ops.write(Types.TAG_KEY, Key.of(value.tagKey()));
        } else {
            Preconditions.checkArgument(mappingType != null, "Cannot write HolderSet with direct ids without a mapping type");
            if (value.ids().length == 1) {
                // Single entries are inlined
                final Key key = ops.context().registryAccess().key(mappingType, value.ids()[0]);
                ops.write(Types.RESOURCE_LOCATION, key);
                return;
            }

            ops.writeList(list -> {
                for (final int id : value.ids()) {
                    final Key key = ops.context().registryAccess().key(mappingType, id);
                    list.write(Types.RESOURCE_LOCATION, key);
                }
            });
        }
    }

    public static final class OptionalHolderSetType extends OptionalType<HolderSet> {

        public OptionalHolderSetType() {
            super(Types.HOLDER_SET);
        }
    }
}
