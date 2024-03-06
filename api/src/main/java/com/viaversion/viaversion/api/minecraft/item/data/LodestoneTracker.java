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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.GlobalPosition;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class LodestoneTracker {

    public static final Type<LodestoneTracker> TYPE = new Type<LodestoneTracker>(LodestoneTracker.class) {
        @Override
        public LodestoneTracker read(final ByteBuf buffer) throws Exception {
            final GlobalPosition position = Type.OPTIONAL_GLOBAL_POSITION.read(buffer);
            final boolean tracked = buffer.readBoolean();
            return new LodestoneTracker(position, tracked);
        }

        @Override
        public void write(final ByteBuf buffer, final LodestoneTracker value) throws Exception {
            Type.OPTIONAL_GLOBAL_POSITION.write(buffer, value.position);
            buffer.writeBoolean(value.tracked);
        }
    };

    private final GlobalPosition position;
    private final boolean tracked;

    public LodestoneTracker(@Nullable final GlobalPosition position, final boolean tracked) {
        this.position = position;
        this.tracked = tracked;
    }

    public @Nullable GlobalPosition pos() {
        return this.position;
    }

    public boolean tracked() {
        return this.tracked;
    }
}
