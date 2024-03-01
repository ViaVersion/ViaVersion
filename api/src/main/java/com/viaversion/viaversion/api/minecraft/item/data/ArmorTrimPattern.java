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

import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public final class ArmorTrimPattern {

    public static final HolderType<ArmorTrimPattern> TYPE = new HolderType<ArmorTrimPattern>() {
        @Override
        public ArmorTrimPattern readDirect(final ByteBuf buffer) throws Exception {
            final String assetName = Type.STRING.read(buffer);
            final int itemId = Type.VAR_INT.readPrimitive(buffer);
            final Tag description = Type.TAG.read(buffer);
            final boolean decal = buffer.readBoolean();
            return new ArmorTrimPattern(assetName, itemId, description, decal);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimPattern value) throws Exception {
            Type.STRING.write(buffer, value.assetName());
            Type.VAR_INT.writePrimitive(buffer, value.itemId());
            Type.TAG.write(buffer, value.description());
            buffer.writeBoolean(value.decal());
        }
    };

    private final String assetName;
    private final int itemId;
    private final Tag description;
    private final boolean decal;

    public ArmorTrimPattern(final String assetName, final int itemId, final Tag description, final boolean decal) {
        this.assetName = assetName;
        this.itemId = itemId;
        this.description = description;
        this.decal = decal;
    }

    public String assetName() {
        return assetName;
    }

    public int itemId() {
        return itemId;
    }

    public Tag description() {
        return description;
    }

    public boolean decal() {
        return decal;
    }
}
