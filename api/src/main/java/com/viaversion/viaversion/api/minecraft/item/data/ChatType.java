/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record ChatType(ChatTypeDecoration chatDecoration, ChatTypeDecoration narrationDecoration) implements Copyable {

    public static final HolderType<ChatType> TYPE = new HolderType<>() {
        @Override
        public ChatType readDirect(final ByteBuf buffer) {
            final ChatTypeDecoration chatDecoration = ChatTypeDecoration.TYPE.read(buffer);
            final ChatTypeDecoration narrationDecoration = ChatTypeDecoration.TYPE.read(buffer);
            return new ChatType(chatDecoration, narrationDecoration);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ChatType value) {
            ChatTypeDecoration.TYPE.write(buffer, value.chatDecoration());
            ChatTypeDecoration.TYPE.write(buffer, value.narrationDecoration());
        }
    };

    @Override
    public ChatType copy() {
        return new ChatType(chatDecoration.copy(), narrationDecoration.copy());
    }

    public record ChatTypeDecoration(String translationKey, int[] parameters, Tag style) implements Copyable {

        public static final Type<ChatTypeDecoration> TYPE = new Type<>(ChatTypeDecoration.class) {

            @Override
            public ChatTypeDecoration read(final ByteBuf buffer) {
                final String translationKey = Types.STRING.read(buffer);
                final int[] parameters = Types.INT_ARRAY_PRIMITIVE.read(buffer);
                final Tag style = Types.TRUSTED_TAG.read(buffer);
                return new ChatTypeDecoration(translationKey, parameters, style);
            }

            @Override
            public void write(final ByteBuf buffer, final ChatTypeDecoration value) {
                Types.STRING.write(buffer, value.translationKey());
                Types.INT_ARRAY_PRIMITIVE.write(buffer, value.parameters());
                Types.TRUSTED_TAG.write(buffer, value.style());
            }
        };

        @Override
        public ChatTypeDecoration copy() {
            return new ChatTypeDecoration(translationKey, parameters.clone(), style.copy());
        }
    }
}
