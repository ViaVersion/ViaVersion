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

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ArmorTrimPattern(String assetName, int itemId, Tag description, boolean decal) implements Copyable, Rewritable {

    public ArmorTrimPattern(final String assetName, final Tag description, final boolean decal) {
        this(assetName, 0, description, decal);
    }

    public static final HolderType<ArmorTrimPattern> TYPE1_20_5 = new HolderType<>() {
        @Override
        public ArmorTrimPattern readDirect(final ByteBuf buffer) {
            final String assetName = Types.STRING.read(buffer);
            final int itemId = Types.VAR_INT.readPrimitive(buffer);
            final Tag description = Types.TAG.read(buffer);
            final boolean decal = buffer.readBoolean();
            return new ArmorTrimPattern(assetName, itemId, description, decal);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimPattern value) {
            Types.STRING.write(buffer, value.assetName());
            Types.VAR_INT.writePrimitive(buffer, value.itemId());
            Types.TAG.write(buffer, value.description());
            buffer.writeBoolean(value.decal());
        }
    };
    public static final HolderType<ArmorTrimPattern> TYPE1_21_5 = new HolderType<>() {
        @Override
        public ArmorTrimPattern readDirect(final ByteBuf buffer) {
            final String assetName = Types.STRING.read(buffer);
            final Tag description = Types.TAG.read(buffer);
            final boolean decal = buffer.readBoolean();
            return new ArmorTrimPattern(assetName, description, decal);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimPattern value) {
            Types.STRING.write(buffer, value.assetName());
            Types.TAG.write(buffer, value.description());
            buffer.writeBoolean(value.decal());
        }
    };

    @Override
    public ArmorTrimPattern rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return new ArmorTrimPattern(assetName, Rewritable.rewriteItem(protocol, clientbound, itemId), description, decal);
    }

    @Override
    public ArmorTrimPattern copy() {
        return new ArmorTrimPattern(assetName, itemId, description.copy(), decal);
    }
}
