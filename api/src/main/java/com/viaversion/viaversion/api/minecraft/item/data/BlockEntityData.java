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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record BlockEntityData(int type, CompoundTag tag) implements Rewritable {

    public static final Type<BlockEntityData> TYPE = new Type<>(BlockEntityData.class) {
        @Override
        public BlockEntityData read(final ByteBuf buffer) {
            final int type = Types.VAR_INT.readPrimitive(buffer);
            final CompoundTag tag = Types.COMPOUND_TAG.read(buffer);
            return new BlockEntityData(type, tag);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockEntityData value) {
            Types.VAR_INT.writePrimitive(buffer, value.type);
            Types.COMPOUND_TAG.write(buffer, value.tag);
        }

        @Override
        public void write(final Ops ops, final BlockEntityData data) {
            ops.writeMap(map -> map
                .write("id", Types.IDENTIFIER, ops.context().registryAccess().blockEntity(data.type))
                .writeInlinedMap(Types.COMPOUND_TAG, data.tag)
            );
        }
    };

    @Override
    public BlockEntityData rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final int mappedType = protocol.getMappingData().getBlockEntityMappings().getNewId(type);
        // Empty mappings might be possible for removed block entities, set a dummy value in that case. Will be handled fine by server and client without losing data
        return new BlockEntityData(mappedType != -1 ? mappedType : 0, tag);
    }
}
