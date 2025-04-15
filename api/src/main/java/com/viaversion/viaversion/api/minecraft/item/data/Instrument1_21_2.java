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
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.EitherHolderType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Instrument1_21_2(Holder<SoundEvent> soundEvent, float useDuration, float range,
                               Tag description) implements Copyable, Rewritable {

    public static final HolderType<Instrument1_21_2> TYPE = new HolderType<>() {
        @Override
        public Instrument1_21_2 readDirect(final ByteBuf buffer) {
            final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            final float useDuration = Types.FLOAT.readPrimitive(buffer);
            final float range = Types.FLOAT.readPrimitive(buffer);
            final Tag description = Types.TAG.read(buffer);
            return new Instrument1_21_2(soundEvent, useDuration, range, description);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final Instrument1_21_2 value) {
            Types.SOUND_EVENT.write(buffer, value.soundEvent());
            Types.FLOAT.writePrimitive(buffer, value.useDuration());
            Types.FLOAT.writePrimitive(buffer, value.range());
            Types.TAG.write(buffer, value.description());
        }
    };
    public static final EitherHolderType<Instrument1_21_2> EITHER_HOLDER_TYPE = new EitherHolderType<>(TYPE);

    @Override
    public Instrument1_21_2 rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final Holder<SoundEvent> soundEvent = this.soundEvent.updateId(Rewritable.soundRewriteFunction(protocol, clientbound));
        return soundEvent == this.soundEvent ? this : new Instrument1_21_2(soundEvent, useDuration, range, description);
    }

    @Override
    public Instrument1_21_2 copy() {
        return new Instrument1_21_2(soundEvent, useDuration, range, description.copy());
    }
}
