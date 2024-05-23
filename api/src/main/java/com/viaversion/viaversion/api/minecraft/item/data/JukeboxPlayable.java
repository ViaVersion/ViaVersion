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

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;

public record JukeboxPlayable(Either<Holder<JukeboxSong>, String> song, boolean showInTooltip) {

    public JukeboxPlayable(final Holder<JukeboxSong> song, final boolean showInTooltip) {
        this(Either.left(song), showInTooltip);
    }

    public JukeboxPlayable(final String resourceKey, final boolean showInTooltip) {
        this(Either.right(resourceKey), showInTooltip);
    }

    public static final Type<JukeboxPlayable> TYPE = new Type<>(JukeboxPlayable.class) {
        @Override
        public JukeboxPlayable read(final ByteBuf buffer) {
            final Either<Holder<JukeboxSong>, String> position = Type.readEither(buffer, JukeboxSong.TYPE, Types.STRING);
            final boolean showInTooltip = buffer.readBoolean();
            return new JukeboxPlayable(position, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final JukeboxPlayable value) {
            Type.writeEither(buffer, value.song, JukeboxSong.TYPE, Types.STRING);
            buffer.writeBoolean(value.showInTooltip);
        }
    };

    public record JukeboxSong(Holder<SoundEvent> soundEvent, Tag description,
                              float lengthInSeconds, int comparatorOutput) {

        public static final HolderType<JukeboxSong> TYPE = new HolderType<>() {
            @Override
            public JukeboxSong readDirect(final ByteBuf buffer) {
                final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
                final Tag description = Types.TAG.read(buffer);
                final float lengthInSeconds = buffer.readFloat();
                final int useDuration = Types.VAR_INT.readPrimitive(buffer);
                return new JukeboxSong(soundEvent, description, lengthInSeconds, useDuration);
            }

            @Override
            public void writeDirect(final ByteBuf buffer, final JukeboxSong value) {
                Types.SOUND_EVENT.write(buffer, value.soundEvent);
                Types.TAG.write(buffer, value.description);
                buffer.writeFloat(value.lengthInSeconds);
                Types.VAR_INT.writePrimitive(buffer, value.comparatorOutput);
            }
        };
    }
}
