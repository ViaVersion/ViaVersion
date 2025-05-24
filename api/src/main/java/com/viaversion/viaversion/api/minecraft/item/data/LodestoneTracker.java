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

import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record LodestoneTracker(@Nullable GlobalBlockPosition position, boolean tracked) {

    public static final Type<LodestoneTracker> TYPE = new Type<>(LodestoneTracker.class) {
        @Override
        public LodestoneTracker read(final ByteBuf buffer) {
            final GlobalBlockPosition position = Types.OPTIONAL_GLOBAL_POSITION.read(buffer);
            final boolean tracked = buffer.readBoolean();
            return new LodestoneTracker(position, tracked);
        }

        @Override
        public void write(final ByteBuf buffer, final LodestoneTracker value) {
            Types.OPTIONAL_GLOBAL_POSITION.write(buffer, value.position);
            buffer.writeBoolean(value.tracked);
        }

        @Override
        public void write(final Ops ops, final LodestoneTracker value) {
            ops.writeMap(map -> map
                .writeOptional("target", Types.GLOBAL_POSITION, value.position)
                .writeOptional("tracked", Types.BOOLEAN, value.tracked, true));
        }
    };
}
