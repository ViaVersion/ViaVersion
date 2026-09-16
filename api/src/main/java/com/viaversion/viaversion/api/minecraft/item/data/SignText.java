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
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record SignText(Tag[] messages, @Nullable Tag[] filteredMessages, int color, boolean hasGlowingText) {

    private static final int LINES = 4;

    public static final Type<SignText> TYPE = new Type<>(SignText.class) {
        @Override
        public SignText read(final ByteBuf buffer) {
            final Tag[] messages = readLines(buffer);
            final Tag[] filteredMessages = buffer.readBoolean() ? readLines(buffer) : null;
            final int color = Types.VAR_INT.readPrimitive(buffer);
            return new SignText(messages, filteredMessages, color, buffer.readBoolean());
        }

        @Override
        public void write(final ByteBuf buffer, final SignText value) {
            writeLines(buffer, value.messages());
            final Tag[] filteredMessages = value.filteredMessages();
            buffer.writeBoolean(filteredMessages != null);
            if (filteredMessages != null) {
                writeLines(buffer, filteredMessages);
            }
            Types.VAR_INT.writePrimitive(buffer, value.color());
            buffer.writeBoolean(value.hasGlowingText());
        }

        private Tag[] readLines(final ByteBuf buffer) {
            final Tag[] lines = new Tag[LINES];
            for (int i = 0; i < LINES; i++) {
                lines[i] = Types.TAG.read(buffer);
            }
            return lines;
        }

        private void writeLines(final ByteBuf buffer, final Tag[] lines) {
            for (int i = 0; i < LINES; i++) {
                Types.TAG.write(buffer, lines[i]);
            }
        }
    };
}
