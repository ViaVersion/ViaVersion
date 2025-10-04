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

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public final class SoundEventType extends HolderType<SoundEvent> {

    public SoundEventType() {
        super(MappingData.MappingType.SOUND);
    }

    @Override
    public SoundEvent readDirect(final ByteBuf buffer) {
        final String resourceLocation = Types.STRING.read(buffer);
        final Float fixedRange = Types.OPTIONAL_FLOAT.read(buffer);
        return new SoundEvent(resourceLocation, fixedRange);
    }

    @Override
    public void writeDirect(final ByteBuf buffer, final SoundEvent value) {
        Types.STRING.write(buffer, value.identifier());
        Types.OPTIONAL_FLOAT.write(buffer, value.fixedRange());
    }

    @Override
    public void writeDirect(final Ops ops, final SoundEvent object) {
        ops.writeMap(map -> map
            .write("sound_id", Types.RESOURCE_LOCATION, Key.of(object.identifier()))
            .writeOptional("range", Types.FLOAT, object.fixedRange()));
    }

    public static final class OptionalSoundEventType extends OptionalHolderType<SoundEvent> {

        public OptionalSoundEventType() {
            super(Types.SOUND_EVENT);
        }
    }
}
