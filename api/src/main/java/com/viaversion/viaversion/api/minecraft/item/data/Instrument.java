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

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public record Instrument(Holder<SoundEvent> soundEvent, int useDuration, float range) {

    public static final HolderType<Instrument> TYPE = new HolderType<>() {
        @Override
        public Instrument readDirect(final ByteBuf buffer) {
            final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            final int useDuration = Types.VAR_INT.readPrimitive(buffer);
            final float range = buffer.readFloat();
            return new Instrument(soundEvent, useDuration, range);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final Instrument value) {
            Types.SOUND_EVENT.write(buffer, value.soundEvent());
            Types.VAR_INT.writePrimitive(buffer, value.useDuration());
            buffer.writeFloat(value.range());
        }
    };

}
