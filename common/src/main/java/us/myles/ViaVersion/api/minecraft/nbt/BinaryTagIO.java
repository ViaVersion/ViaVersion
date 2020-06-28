/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
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
package us.myles.ViaVersion.api.minecraft.nbt;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * See https://github.com/KyoriPowered/adventure.
 */
public final class BinaryTagIO {
    private BinaryTagIO() {
    }

    /**
     * Reads a compound tag from a {@link String}.
     *
     * @param input the string
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NotNull
    CompoundTag readString(final @NotNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
            final CompoundTag tag = parser.compound();
            if (buffer.skipWhitespace().hasMore()) {
                throw new IOException("Document had trailing content after first CompoundTag");
            }
            return tag;
        } catch (final StringTagParseException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Writes a compound tag to a {@link String}.
     *
     * @param tag the compound tag
     * @return the string
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static @NotNull
    String writeString(final @NotNull CompoundTag tag) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (final TagStringWriter emit = new TagStringWriter(sb)) {
            emit.writeTag(tag);
        }
        return sb.toString();
    }
}