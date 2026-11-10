/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import io.netty.buffer.ByteBuf;

public record MobVisibility(HolderSet targetingEntityTypes, float visibility) {

    public static final Type<MobVisibility> TYPE = new Type<>(MobVisibility.class) {
        @Override
        public MobVisibility read(final ByteBuf buffer) {
            final HolderSet targetingEntityTypes = Types.HOLDER_SET.read(buffer);
            final float visibility = Types.FLOAT.readPrimitive(buffer);
            return new MobVisibility(targetingEntityTypes, visibility);
        }

        @Override
        public void write(final ByteBuf buffer, final MobVisibility value) {
            Types.HOLDER_SET.write(buffer, value.targetingEntityTypes);
            Types.FLOAT.writePrimitive(buffer, value.visibility);
        }

        @Override
        public void write(final Ops ops, final MobVisibility value) {
            ops.writeMap(map -> map
                .write("targeting_entity_types", new HolderSetType(MappingData.MappingType.ENTITY_TYPE), value.targetingEntityTypes)
                .write("visibility", Types.FLOAT, value.visibility));
        }
    };
}
