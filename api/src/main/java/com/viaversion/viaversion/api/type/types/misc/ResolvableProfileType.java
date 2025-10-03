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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.ResolvableProfile;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class ResolvableProfileType extends Type<ResolvableProfile> {

    public ResolvableProfileType() {
        super(ResolvableProfile.class);
    }

    @Override
    public ResolvableProfile read(final ByteBuf buffer) {
        final GameProfile profile;
        if (buffer.readBoolean()) {
            final UUID id = Types.UUID.read(buffer);
            final String name = Types.STRING.read(buffer);
            final GameProfile.Property[] properties = Types.PROFILE_PROPERTY_ARRAY.read(buffer);
            profile = new GameProfile(name, id, properties, false);
        } else {
            profile = Types.GAME_PROFILE.read(buffer);
        }

        final String texture = Types.OPTIONAL_STRING.read(buffer);
        final String capeTexture = Types.OPTIONAL_STRING.read(buffer);
        final String elytraTexture = Types.OPTIONAL_STRING.read(buffer);
        final Boolean encodedModelType = Types.OPTIONAL_BOOLEAN.read(buffer); // ...
        final Integer modelType = encodedModelType != null ? (encodedModelType ? 0 : 1) : null;
        return new ResolvableProfile(profile, texture, capeTexture, elytraTexture, modelType);
    }

    @Override
    public void write(final ByteBuf buffer, final ResolvableProfile value) {
        final GameProfile profile = value.profile();
        if (!profile.dynamic()) {
            buffer.writeBoolean(true);
            Types.UUID.write(buffer, profile.id());
            Types.STRING.write(buffer, profile.name());
            Types.PROFILE_PROPERTY_ARRAY.write(buffer, profile.properties());
        } else {
            buffer.writeBoolean(false);
            Types.GAME_PROFILE.write(buffer, profile);
        }

        Types.OPTIONAL_STRING.write(buffer, value.bodyTexture());
        Types.OPTIONAL_STRING.write(buffer, value.capeTexture());
        Types.OPTIONAL_STRING.write(buffer, value.elytraTexture());
        Types.OPTIONAL_BOOLEAN.write(buffer, value.modelType() != null ? value.modelType() == 0 : null);
    }

    @Override
    public void write(final Ops ops, final ResolvableProfile value) {
        final String modelType = value.modelType() != null ? (value.modelType() == 0 ? "slim" : "wide") : null;
        ops.writeMap(map -> map
            .writeInlinedMap(Types.GAME_PROFILE, value.profile())
            .writeOptional("texture", Types.RESOURCE_LOCATION, value.bodyTexture() != null ? Key.of(value.bodyTexture()) : null)
            .writeOptional("cape", Types.RESOURCE_LOCATION, value.capeTexture() != null ? Key.of(value.capeTexture()) : null)
            .writeOptional("elytra", Types.RESOURCE_LOCATION, value.elytraTexture() != null ? Key.of(value.elytraTexture()) : null)
            .writeOptional("model", Types.OPTIONAL_STRING, modelType)
        );
    }
};
