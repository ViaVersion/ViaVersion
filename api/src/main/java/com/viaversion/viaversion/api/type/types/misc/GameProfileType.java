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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public final class GameProfileType extends Type<GameProfile> {

    public GameProfileType() {
        super(GameProfile.class);
    }

    @Override
    public GameProfile read(final ByteBuf buffer) throws Exception {
        final String name = Type.OPTIONAL_STRING.read(buffer);
        final java.util.UUID id = Type.OPTIONAL_UUID.read(buffer);
        final int propertyCount = Type.VAR_INT.readPrimitive(buffer);
        final GameProfile.Property[] properties = new GameProfile.Property[propertyCount];
        for (int i = 0; i < propertyCount; i++) {
            final String propertyName = Type.STRING.read(buffer);
            final String propertyValue = Type.STRING.read(buffer);
            final String propertySignature = Type.OPTIONAL_STRING.read(buffer);
            properties[i] = new GameProfile.Property(propertyName, propertyValue, propertySignature);
        }
        return new GameProfile(name, id, properties);
    }

    @Override
    public void write(final ByteBuf buffer, final GameProfile value) throws Exception {
        Type.OPTIONAL_STRING.write(buffer, value.name());
        Type.OPTIONAL_UUID.write(buffer, value.id());
        Type.VAR_INT.writePrimitive(buffer, value.properties().length);
        for (final GameProfile.Property property : value.properties()) {
            Type.STRING.write(buffer, property.name());
            Type.STRING.write(buffer, property.value());
            Type.OPTIONAL_STRING.write(buffer, property.signature());
        }
    }
}
