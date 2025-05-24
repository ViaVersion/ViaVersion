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
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) implements Rewritable {

    public static final Type<UseCooldown> TYPE = new Type<>(UseCooldown.class) {
        @Override
        public UseCooldown read(final ByteBuf buffer) {
            final float seconds = buffer.readFloat();
            final String cooldownGroup = Types.OPTIONAL_STRING.read(buffer);
            return new UseCooldown(seconds, cooldownGroup);
        }

        @Override
        public void write(final ByteBuf buffer, final UseCooldown value) {
            buffer.writeFloat(value.seconds());
            Types.OPTIONAL_STRING.write(buffer, value.cooldownGroup());
        }

        @Override
        public void write(final Ops ops, final UseCooldown value) {
            final Key cooldownGroup = value.cooldownGroup != null ? Key.of(value.cooldownGroup) : null;
            ops.writeMap(map -> map
                .write("seconds", Types.FLOAT, value.seconds())
                .writeOptional("cooldown_group", Types.RESOURCE_LOCATION, cooldownGroup));
        }
    };

    @Override
    public UseCooldown rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        if (cooldownGroup == null) {
            return this;
        }

        final String mappedCooldownGroup = Rewritable.rewriteItem(protocol, clientbound, cooldownGroup);
        return new UseCooldown(seconds, mappedCooldownGroup);
    }
}
