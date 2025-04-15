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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.EitherHolderType;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ProvidesTrimMaterial(EitherHolder<ArmorTrimMaterial> material) implements Rewritable {

    public static final Type<ProvidesTrimMaterial> TYPE = new Type<>(ProvidesTrimMaterial.class) {

        @Override
        public ProvidesTrimMaterial read(final ByteBuf buffer) {
            final EitherHolder<ArmorTrimMaterial> position = EitherHolderType.read(buffer, ArmorTrimMaterial.TYPE1_21_5);
            return new ProvidesTrimMaterial(position);
        }

        @Override
        public void write(final ByteBuf buffer, final ProvidesTrimMaterial value) {
            EitherHolderType.write(buffer, value.material, ArmorTrimMaterial.TYPE1_21_5);
        }
    };

    @Override
    public ProvidesTrimMaterial rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        if (material.hasKey() || material.holder().hasId()) {
            return this;
        }

        final ArmorTrimMaterial trimMaterial = material.holder().value();
        return new ProvidesTrimMaterial(EitherHolder.of(Holder.of(trimMaterial.rewrite(connection, protocol, clientbound))));
    }
}
