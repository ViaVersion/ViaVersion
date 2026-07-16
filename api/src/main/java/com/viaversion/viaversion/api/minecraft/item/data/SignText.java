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
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record SignText(Tag[] messages, Tag @Nullable [] filteredMessages, int color, boolean hasGlowingText) {

    public static final Type<SignText> TYPE = new Type<>(SignText.class) {
        @Override
        public SignText read(final ByteBuf buffer) {
            final Tag[] messages = Types.SIGN_TEXT.read(buffer);
            final Tag[] filteredMessages = Types.OPTIONAL_SIGN_TEXT.read(buffer);
            final int color = Types.VAR_INT.readPrimitive(buffer);
            final boolean hasGlowingText = buffer.readBoolean();
            return new SignText(messages, filteredMessages, color, hasGlowingText);
        }

        @Override
        public void write(final ByteBuf buffer, final SignText value) {
            Types.SIGN_TEXT.write(buffer, value.messages);
            Types.OPTIONAL_SIGN_TEXT.write(buffer, value.filteredMessages);
            Types.VAR_INT.writePrimitive(buffer, value.color);
            Types.BOOLEAN.write(buffer, value.hasGlowingText);
        }

        @Override
        public void write(final Ops ops, final SignText value) {
            ops.writeMap(map -> map
                .write("messages", Types.SIGN_TEXT, value.messages)
                .writeOptional("filtered_messages", Types.SIGN_TEXT, value.filteredMessages)
                .write("color", Types.VAR_INT, value.color, 15)
                .write("has_glowing_text", Types.BOOLEAN, value.hasGlowingText, false));
        }
    };
}
