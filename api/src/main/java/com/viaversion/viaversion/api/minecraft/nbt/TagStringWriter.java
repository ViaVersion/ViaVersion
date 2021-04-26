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
package com.viaversion.viaversion.api.minecraft.nbt;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * See https://github.com/KyoriPowered/adventure.
 */
/* package */ final class TagStringWriter implements AutoCloseable {
    private final Appendable out;
    private final String indent = "  "; // TODO: pretty-printing
    private int level;
    /**
     * Whether a {@link Tokens#VALUE_SEPARATOR} needs to be printed before the beginning of the next object.
     */
    private boolean needsSeparator;

    public TagStringWriter(final Appendable out) {
        this.out = out;
    }

    // NBT-specific

    public TagStringWriter writeTag(final Tag tag) throws IOException {
        if (tag instanceof CompoundTag) {
            return this.writeCompound((CompoundTag) tag);
        } else if (tag instanceof ListTag) {
            return this.writeList((ListTag) tag);
        } else if (tag instanceof ByteArrayTag) {
            return this.writeByteArray((ByteArrayTag) tag);
        } else if (tag instanceof IntArrayTag) {
            return this.writeIntArray((IntArrayTag) tag);
        } else if (tag instanceof LongArrayTag) {
            return this.writeLongArray((LongArrayTag) tag);
        } else if (tag instanceof StringTag) {
            return this.value(((StringTag) tag).getValue(), Tokens.EOF);
        } else if (tag instanceof ByteTag) {
            return this.value(Byte.toString(((ByteTag) tag).asByte()), Tokens.TYPE_BYTE);
        } else if (tag instanceof ShortTag) {
            return this.value(Short.toString(((ShortTag) tag).asShort()), Tokens.TYPE_SHORT);
        } else if (tag instanceof IntTag) {
            return this.value(Integer.toString(((IntTag) tag).asInt()), Tokens.TYPE_INT);
        } else if (tag instanceof LongTag) {
            return this.value(Long.toString(((LongTag) tag).asLong()), Tokens.TYPE_LONG);
        } else if (tag instanceof FloatTag) {
            return this.value(Float.toString(((FloatTag) tag).asFloat()), Tokens.TYPE_FLOAT);
        } else if (tag instanceof DoubleTag) {
            return this.value(Double.toString(((DoubleTag) tag).asDouble()), Tokens.TYPE_DOUBLE);
        } else {
            throw new IOException("Unknown tag type: " + tag.getClass().getSimpleName());
            // unknown!
        }
    }

    private TagStringWriter writeCompound(final CompoundTag tag) throws IOException {
        this.beginCompound();
        for (Map.Entry<String, Tag> entry : tag.entrySet()) {
            this.key(entry.getKey());
            this.writeTag(entry.getValue());
        }
        this.endCompound();
        return this;
    }

    private TagStringWriter writeList(final ListTag tag) throws IOException {
        this.beginList();
        for (final Tag el : tag) {
            this.printAndResetSeparator();
            this.writeTag(el);
        }
        this.endList();
        return this;
    }

    private TagStringWriter writeByteArray(final ByteArrayTag tag) throws IOException {
        this.beginArray(Tokens.TYPE_BYTE);

        final byte[] value = tag.getValue();
        for (int i = 0, length = value.length; i < length; i++) {
            this.printAndResetSeparator();
            this.value(Byte.toString(value[i]), Tokens.TYPE_BYTE);
        }
        this.endArray();
        return this;
    }

    private TagStringWriter writeIntArray(final IntArrayTag tag) throws IOException {
        this.beginArray(Tokens.TYPE_INT);

        final int[] value = tag.getValue();
        for (int i = 0, length = value.length; i < length; i++) {
            this.printAndResetSeparator();
            this.value(Integer.toString(value[i]), Tokens.TYPE_INT);
        }
        this.endArray();
        return this;
    }

    private TagStringWriter writeLongArray(final LongArrayTag tag) throws IOException {
        this.beginArray(Tokens.TYPE_LONG);

        final long[] value = tag.getValue();
        for (int i = 0, length = value.length; i < length; i++) {
            this.printAndResetSeparator();
            this.value(Long.toString(value[i]), Tokens.TYPE_LONG);
        }
        this.endArray();
        return this;
    }

    // Value types

    public TagStringWriter beginCompound() throws IOException {
        this.printAndResetSeparator();
        this.level++;
        this.out.append(Tokens.COMPOUND_BEGIN);
        return this;
    }

    public TagStringWriter endCompound() throws IOException {
        this.out.append(Tokens.COMPOUND_END);
        this.level--;
        this.needsSeparator = true;
        return this;
    }

    public TagStringWriter key(final String key) throws IOException {
        this.printAndResetSeparator();
        this.writeMaybeQuoted(key, false);
        this.out.append(Tokens.COMPOUND_KEY_TERMINATOR); // TODO: spacing/pretty-printing
        return this;
    }

    public TagStringWriter value(final String value, final char valueType) throws IOException {
        if (valueType == Tokens.EOF) { // string doesn't have its type
            this.writeMaybeQuoted(value, true);
        } else {
            this.out.append(value);
            if (valueType != Tokens.TYPE_INT) {
                this.out.append(valueType);
            }
        }
        this.needsSeparator = true;
        return this;
    }

    public TagStringWriter beginList() throws IOException {
        this.printAndResetSeparator();
        this.level++;
        this.out.append(Tokens.ARRAY_BEGIN);
        return this;
    }

    public TagStringWriter endList() throws IOException {
        this.out.append(Tokens.ARRAY_END);
        this.level--;
        this.needsSeparator = true;
        return this;
    }

    private TagStringWriter beginArray(final char type) throws IOException {
        this.beginList()
                .out.append(type)
                .append(Tokens.ARRAY_SIGNATURE_SEPARATOR);
        return this;
    }

    private TagStringWriter endArray() throws IOException {
        return this.endList();
    }

    private void writeMaybeQuoted(final String content, boolean requireQuotes) throws IOException {
        if (!requireQuotes) {
            for (int i = 0; i < content.length(); ++i) {
                if (!Tokens.id(content.charAt(i))) {
                    requireQuotes = true;
                    break;
                }
            }
        }
        if (requireQuotes) { // TODO: single quotes
            this.out.append(Tokens.DOUBLE_QUOTE);
            this.out.append(escape(content, Tokens.DOUBLE_QUOTE));
            this.out.append(Tokens.DOUBLE_QUOTE);
        } else {
            this.out.append(content);
        }
    }

    private static String escape(final String content, final char quoteChar) {
        final StringBuilder output = new StringBuilder(content.length());
        for (int i = 0; i < content.length(); ++i) {
            final char c = content.charAt(i);
            if (c == quoteChar || c == '\\') {
                output.append(Tokens.ESCAPE_MARKER);
            }
            output.append(c);
        }
        return output.toString();
    }

    private void printAndResetSeparator() throws IOException {
        if (this.needsSeparator) {
            this.out.append(Tokens.VALUE_SEPARATOR);
            this.needsSeparator = false;
        }
    }


    @Override
    public void close() throws IOException {
        if (this.level != 0) {
            throw new IllegalStateException("Document finished with unbalanced start and end objects");
        }
        if (this.out instanceof Writer) {
            ((Writer) this.out).flush();
        }
    }
}
