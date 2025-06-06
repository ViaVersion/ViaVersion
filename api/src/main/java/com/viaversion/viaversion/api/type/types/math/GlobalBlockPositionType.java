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
package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class GlobalBlockPositionType extends Type<GlobalBlockPosition> {

    public GlobalBlockPositionType() {
        super(GlobalBlockPosition.class);
    }

    @Override
    public GlobalBlockPosition read(ByteBuf buffer) {
        final String dimension = Types.STRING.read(buffer);
        return Types.BLOCK_POSITION1_14.read(buffer).withDimension(dimension);
    }

    @Override
    public void write(ByteBuf buffer, GlobalBlockPosition object) {
        Types.STRING.write(buffer, object.dimension());
        Types.BLOCK_POSITION1_14.write(buffer, object);
    }

    @Override
    public void write(final Ops ops, final GlobalBlockPosition value) {
        ops.writeMap(map -> map
            .write("dimension", Types.RESOURCE_LOCATION, Key.of(value.dimension()))
            .write("pos", Types.BLOCK_POSITION1_14, value));
    }

    public static final class OptionalGlobalPositionType extends OptionalType<GlobalBlockPosition> {

        public OptionalGlobalPositionType() {
            super(Types.GLOBAL_POSITION);
        }
    }
}
