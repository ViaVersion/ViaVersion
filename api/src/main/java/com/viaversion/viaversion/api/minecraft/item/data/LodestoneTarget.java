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

public final class LodestoneTarget {

    public static final Type<LodestoneTarget> TYPE = new Type<LodestoneTarget>(LodestoneTarget.class) {
        @Override
        public LodestoneTarget read(final ByteBuf buffer) throws Exception {
            final GlobalPosition position = Type.GLOBAL_POSITION.read(buffer);
            final boolean tracked = buffer.readBoolean();
            return new LodestoneTarget(position, tracked);
        }

        @Override
        public void write(final ByteBuf buffer, final LodestoneTarget value) throws Exception {
            Type.GLOBAL_POSITION.write(buffer, value.position);
            buffer.writeBoolean(value.tracked);
        }
    };

    private final GlobalPosition position;
    private final boolean tracked;

    public LodestoneTarget(final GlobalPosition position, final boolean tracked) {
        this.position = position;
        this.tracked = tracked;
    }

    public GlobalPosition pos() {
        return this.position;
    }

    public boolean tracked() {
        return this.tracked;
    }
}
