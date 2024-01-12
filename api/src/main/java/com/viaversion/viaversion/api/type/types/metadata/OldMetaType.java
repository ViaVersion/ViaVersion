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
package com.viaversion.viaversion.api.type.types.metadata;

import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.metadata.MetaTypeTemplate;
import io.netty.buffer.ByteBuf;

public abstract class OldMetaType extends MetaTypeTemplate {
    private static final int END = 127;

    @Override
    public Metadata read(final ByteBuf buffer) throws Exception {
        final byte index = buffer.readByte();
        if (index == END) return null; // End of metadata
        final MetaType type = this.getType((index & 224) >> 5);
        return new Metadata(index & 31, type, type.type().read(buffer));
    }

    protected abstract MetaType getType(final int index);

    @Override
    public void write(final ByteBuf buffer, final Metadata object) throws Exception {
        if (object == null) {
            buffer.writeByte(END);
        } else {
            final int index = (object.metaType().typeId() << 5 | object.id() & 31) & 255;
            buffer.writeByte(index);
            object.metaType().type().write(buffer, object.getValue());
        }
    }
}
