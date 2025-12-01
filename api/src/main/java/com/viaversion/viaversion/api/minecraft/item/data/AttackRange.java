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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record AttackRange(float minRange, float maxRange,
                          float minCreativeRange, float maxCreativeRange,
                          float hitboxMargin, float mobFactor) {

    public static final Type<AttackRange> TYPE = new Type<>(AttackRange.class) {
        @Override
        public AttackRange read(final ByteBuf buffer) {
            final float minRange = Types.FLOAT.readPrimitive(buffer);
            final float maxRange = Types.FLOAT.readPrimitive(buffer);
            final float minCreativeRange = Types.FLOAT.readPrimitive(buffer);
            final float maxCreativeRange = Types.FLOAT.readPrimitive(buffer);
            final float hitboxMargin = Types.FLOAT.readPrimitive(buffer);
            final float mobFactor = Types.FLOAT.readPrimitive(buffer);
            return new AttackRange(minRange, maxRange, minCreativeRange, maxCreativeRange, hitboxMargin, mobFactor);
        }

        @Override
        public void write(final ByteBuf buffer, final AttackRange value) {
            Types.FLOAT.writePrimitive(buffer, value.minRange);
            Types.FLOAT.writePrimitive(buffer, value.maxRange);
            Types.FLOAT.writePrimitive(buffer, value.minCreativeRange);
            Types.FLOAT.writePrimitive(buffer, value.maxCreativeRange);
            Types.FLOAT.writePrimitive(buffer, value.hitboxMargin);
            Types.FLOAT.writePrimitive(buffer, value.mobFactor);
        }

        @Override
        public void write(final Ops ops, final AttackRange AttackRange) {
            ops.writeMap(map -> map
                .writeOptional("min_range", Types.FLOAT, AttackRange.minRange, 0F)
                .writeOptional("max_range", Types.FLOAT, AttackRange.maxRange, 3F)
                .writeOptional("min_creative_reach", Types.FLOAT, AttackRange.minCreativeRange, 0F)
                .writeOptional("max_creative_reach", Types.FLOAT, AttackRange.maxCreativeRange, 5F)
                .writeOptional("hitbox_margin", Types.FLOAT, AttackRange.hitboxMargin, 0.3F)
                .writeOptional("mob_factor", Types.FLOAT, AttackRange.mobFactor, 1F));
        }
    };
}
