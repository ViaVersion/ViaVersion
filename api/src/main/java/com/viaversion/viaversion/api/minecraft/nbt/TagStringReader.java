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
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * See https://github.com/KyoriPowered/adventure.
 */
/* package */ final class TagStringReader {
    private final CharBuffer buffer;

    public TagStringReader(final CharBuffer buffer) {
        this.buffer = buffer;
    }

    public CompoundTag compound() throws StringTagParseException {
        this.buffer.expect(Tokens.COMPOUND_BEGIN);
        final CompoundTag compoundTag = new CompoundTag();
        if (this.buffer.peek() == Tokens.COMPOUND_END) {
            this.buffer.take();
            return compoundTag;
        }

        while (this.buffer.hasMore()) {
            final String key = this.key();
            final Tag tag = this.tag();
            compoundTag.put(key, tag);
            if (this.separatorOrCompleteWith(Tokens.COMPOUND_END)) {
                return compoundTag;
            }
        }
        throw this.buffer.makeError("Unterminated compound tag!");
    }

    public ListTag list() throws StringTagParseException {
        final ListTag listTag = new ListTag();
        this.buffer.expect(Tokens.ARRAY_BEGIN);
        final boolean prefixedIndex = this.buffer.peek() == '0' && this.buffer.peek(1) == ':';
        while (this.buffer.hasMore()) {
            if (this.buffer.peek() == Tokens.ARRAY_END) {
                this.buffer.advance();
                return listTag;
            }

            if (prefixedIndex) {
                this.buffer.takeUntil(':');
            }

            final Tag next = this.tag();
            // TODO: validate type
            listTag.add(next);
            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                return listTag;
            }
        }
        throw this.buffer.makeError("Reached end of file without end of list tag!");
    }

    /**
     * Similar to a list tag in syntax, but returning a single array tag rather than a list of tags.
     *
     * @return array-typed tag
     */
    public Tag array(final char elementType) throws StringTagParseException {
        this.buffer.expect(Tokens.ARRAY_BEGIN)
                .expect(elementType)
                .expect(Tokens.ARRAY_SIGNATURE_SEPARATOR);

        if (elementType == Tokens.TYPE_BYTE) {
            return new ByteArrayTag(this.byteArray());
        } else if (elementType == Tokens.TYPE_INT) {
            return new IntArrayTag(this.intArray());
        } else if (elementType == Tokens.TYPE_LONG) {
            return new LongArrayTag(this.longArray());
        } else {
            throw this.buffer.makeError("Type " + elementType + " is not a valid element type in an array!");
        }
    }

    private byte[] byteArray() throws StringTagParseException {
        final List<Byte> bytes = new ArrayList<>();
        while (this.buffer.hasMore()) {
            final CharSequence value = this.buffer.skipWhitespace().takeUntil(Tokens.TYPE_BYTE);
            try {
                bytes.add(Byte.valueOf(value.toString()));
            } catch (final NumberFormatException ex) {
                throw this.buffer.makeError("All elements of a byte array must be bytes!");
            }

            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                final byte[] result = new byte[bytes.size()];
                for (int i = 0; i < bytes.size(); ++i) { // todo yikes, let's do less boxing
                    result[i] = bytes.get(i);
                }
                return result;
            }
        }
        throw this.buffer.makeError("Reached end of document without array close");
    }

    private int[] intArray() throws StringTagParseException {
        final IntStream.Builder builder = IntStream.builder();
        while (this.buffer.hasMore()) {
            final Tag value = this.tag();
            if (!(value instanceof IntTag)) {
                throw this.buffer.makeError("All elements of an int array must be ints!");
            }
            builder.add(((NumberTag) value).asInt());
            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                return builder.build().toArray();
            }
        }
        throw this.buffer.makeError("Reached end of document without array close");
    }

    private long[] longArray() throws StringTagParseException {
        final List<Long> longs = new ArrayList<>();
        while (this.buffer.hasMore()) {
            final CharSequence value = this.buffer.skipWhitespace().takeUntil(Tokens.TYPE_LONG);
            try {
                longs.add(Long.valueOf(value.toString()));
            } catch (final NumberFormatException ex) {
                throw this.buffer.makeError("All elements of a long array must be longs!");
            }

            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                final long[] result = new long[longs.size()];
                for (int i = 0; i < longs.size(); ++i) { // todo yikes
                    result[i] = longs.get(i);
                }
                return result;
            }
        }
        throw this.buffer.makeError("Reached end of document without array close");
    }

    public String key() throws StringTagParseException {
        this.buffer.skipWhitespace();
        final char starChar = this.buffer.peek();
        try {
            if (starChar == Tokens.SINGLE_QUOTE || starChar == Tokens.DOUBLE_QUOTE) {
                return unescape(this.buffer.takeUntil(this.buffer.take()).toString());
            }

            final StringBuilder builder = new StringBuilder();
            while (this.buffer.peek() != ':') { // DO NOT CHECK FOR CHARACTER VALIDITY; LEGACY NBT ALLOWS ANY CHARACTER, EVEN WHEN UNQUOTED
                builder.append(this.buffer.take());
            }
            return builder.toString();
        } finally {
            this.buffer.expect(Tokens.COMPOUND_KEY_TERMINATOR);
        }
    }

    public Tag tag() throws StringTagParseException {
        final char startToken = this.buffer.skipWhitespace().peek();
        switch (startToken) {
            case Tokens.COMPOUND_BEGIN:
                return this.compound();
            case Tokens.ARRAY_BEGIN:
                if (this.buffer.peek(2) == ';') { // we know we're an array tag
                    return this.array(this.buffer.peek(1));
                } else {
                    return this.list();
                }
            case Tokens.SINGLE_QUOTE:
            case Tokens.DOUBLE_QUOTE:
                // definitely a string tag
                this.buffer.advance();
                return new StringTag(unescape(this.buffer.takeUntil(startToken).toString()));
            default: // scalar
                return this.scalar();
        }
    }

    /**
     * A tag that is definitely some sort of scalar
     *
     * <p>Does not detect quoted strings, so </p>
     *
     * @return a parsed tag
     */
    private Tag scalar() {
        final StringBuilder builder = new StringBuilder();
        boolean possiblyNumeric = true;
        while (this.buffer.hasMore()) {
            final char current = this.buffer.peek();
            if (possiblyNumeric && !Tokens.numeric(current)) {
                if (builder.length() != 0) {
                    Tag result = null;
                    try {
                        switch (Character.toUpperCase(current)) { // try to read and return as a number
                            // case Tokens.TYPE_INTEGER: // handled below, ints are ~special~
                            case Tokens.TYPE_BYTE:
                                result = new ByteTag(Byte.parseByte(builder.toString()));
                                break;
                            case Tokens.TYPE_SHORT:
                                result = new ShortTag(Short.parseShort(builder.toString()));
                                break;
                            case Tokens.TYPE_LONG:
                                result = new LongTag(Long.parseLong(builder.toString()));
                                break;
                            case Tokens.TYPE_FLOAT:
                                result = new FloatTag(Float.parseFloat(builder.toString()));
                                break;
                            case Tokens.TYPE_DOUBLE:
                                result = new DoubleTag(Double.parseDouble(builder.toString()));
                                break;
                        }
                    } catch (final NumberFormatException ex) {
                        possiblyNumeric = false; // fallback to treating as a String
                    }
                    if (result != null) {
                        this.buffer.take();
                        return result;
                    }
                }
            }
            if (current == '\\') { // escape -- we are significantly more lenient than original format at the moment
                this.buffer.advance();
                builder.append(this.buffer.take());
            } else if (Tokens.id(current)) {
                builder.append(this.buffer.take());
            } else { // end of value
                break;
            }
        }
        // if we run out of content without an explicit value separator, then we're either an integer or string tag -- all others have a character at the end
        final String built = builder.toString();
        if (possiblyNumeric) {
            try {
                return new IntTag(Integer.parseInt(built));
            } catch (final NumberFormatException ex) {
                // ignore
            }
        }
        return new StringTag(built);

    }

    private boolean separatorOrCompleteWith(final char endCharacter) throws StringTagParseException {
        if (this.buffer.skipWhitespace().peek() == endCharacter) {
            this.buffer.take();
            return true;
        }
        this.buffer.expect(Tokens.VALUE_SEPARATOR);
        if (this.buffer.skipWhitespace().peek() == endCharacter) {
            this.buffer.take();
            return true;
        }
        return false;
    }


    /**
     * Remove simple escape sequences from a string
     *
     * @param withEscapes input string with escapes
     * @return string with escapes processed
     */
    private static String unescape(final String withEscapes) {
        int escapeIdx = withEscapes.indexOf(Tokens.ESCAPE_MARKER);
        if (escapeIdx == -1) { // nothing to unescape
            return withEscapes;
        }
        int lastEscape = 0;
        final StringBuilder output = new StringBuilder(withEscapes.length());
        do {
            output.append(withEscapes, lastEscape, escapeIdx);
            lastEscape = escapeIdx + 1;
        } while ((escapeIdx = withEscapes.indexOf(Tokens.ESCAPE_MARKER, lastEscape + 1)) != -1); // add one extra character to make sure we don't include escaped backslashes
        output.append(withEscapes.substring(lastEscape));
        return output.toString();
    }
}
