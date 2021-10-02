/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2021 KyoriPowered
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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

// Specific Via changes:
// - Use OpenNBT tags
// - Small byteArray() optimization
// - acceptLegacy = true by default
// - Don't parse value as DoubleTag when possiblyNumeric
// - Fix trailing comma reading in compounds, lists, and arrays
final class TagStringReader {
    private static final int MAX_DEPTH = 512;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];

    private final CharBuffer buffer;
    private boolean acceptLegacy = true; // Via - always true
    private int depth;

    TagStringReader(final CharBuffer buffer) {
        this.buffer = buffer;
    }

    public CompoundTag compound() throws StringTagParseException {
        this.buffer.expect(Tokens.COMPOUND_BEGIN);
        final CompoundTag compoundTag = new CompoundTag();
        if (this.buffer.takeIf(Tokens.COMPOUND_END)) {
            return compoundTag;
        }

        while (this.buffer.hasMore()) {
            compoundTag.put(this.key(), this.tag());
            if (this.separatorOrCompleteWith(Tokens.COMPOUND_END)) {
                return compoundTag;
            }
        }
        throw this.buffer.makeError("Unterminated compound tag!");
    }

    public ListTag list() throws StringTagParseException {
        final ListTag listTag = new ListTag();
        this.buffer.expect(Tokens.ARRAY_BEGIN);
        final boolean prefixedIndex = this.acceptLegacy && this.buffer.peek() == '0' && this.buffer.peek(1) == ':';
        if (!prefixedIndex && this.buffer.takeIf(Tokens.ARRAY_END)) {
            return listTag;
        }
        while (this.buffer.hasMore()) {
            if (prefixedIndex) {
                this.buffer.takeUntil(':');
            }

            final Tag next = this.tag();
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
    public Tag array(char elementType) throws StringTagParseException {
        this.buffer.expect(Tokens.ARRAY_BEGIN)
                .expect(elementType)
                .expect(Tokens.ARRAY_SIGNATURE_SEPARATOR);

        elementType = Character.toLowerCase(elementType);
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
        if (this.buffer.takeIf(Tokens.ARRAY_END)) {
            return EMPTY_BYTE_ARRAY;
        }

        final IntList bytes = new IntArrayList(); // Via - no boxing
        while (this.buffer.hasMore()) {
            final CharSequence value = this.buffer.skipWhitespace().takeUntil(Tokens.TYPE_BYTE);
            try {
                bytes.add(Byte.parseByte(value.toString())); // Via
            } catch (final NumberFormatException ex) {
                throw this.buffer.makeError("All elements of a byte array must be bytes!");
            }

            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                final byte[] result = new byte[bytes.size()];
                for (int i = 0; i < bytes.size(); ++i) {
                    result[i] = (byte) bytes.getInt(i); // Via
                }
                return result;
            }
        }
        throw this.buffer.makeError("Reached end of document without array close");
    }

    private int[] intArray() throws StringTagParseException {
        if (this.buffer.takeIf(Tokens.ARRAY_END)) {
            return EMPTY_INT_ARRAY;
        }

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
        if (this.buffer.takeIf(Tokens.ARRAY_END)) {
            return EMPTY_LONG_ARRAY;
        }

        final LongStream.Builder longs = LongStream.builder();
        while (this.buffer.hasMore()) {
            final CharSequence value = this.buffer.skipWhitespace().takeUntil(Tokens.TYPE_LONG);
            try {
                longs.add(Long.parseLong(value.toString()));
            } catch (final NumberFormatException ex) {
                throw this.buffer.makeError("All elements of a long array must be longs!");
            }

            if (this.separatorOrCompleteWith(Tokens.ARRAY_END)) {
                return longs.build().toArray();
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
            while (this.buffer.hasMore()) {
                final char peek = this.buffer.peek();
                if (!Tokens.id(peek)) {
                    if (this.acceptLegacy) {
                        // In legacy format, a key is any non-colon character, with escapes allowed
                        if (peek == Tokens.ESCAPE_MARKER) {
                            this.buffer.take(); // skip
                            continue;
                        } else if (peek != Tokens.COMPOUND_KEY_TERMINATOR) {
                            builder.append(this.buffer.take());
                            continue;
                        }
                    }
                    break;
                }
                builder.append(this.buffer.take());
            }
            return builder.toString();
        } finally {
            this.buffer.expect(Tokens.COMPOUND_KEY_TERMINATOR);
        }
    }

    public Tag tag() throws StringTagParseException {
        if (this.depth++ > MAX_DEPTH) {
            throw this.buffer.makeError("Exceeded maximum allowed depth of " + MAX_DEPTH + " when reading tag");
        }
        try {
            final char startToken = this.buffer.skipWhitespace().peek();
            switch (startToken) {
                case Tokens.COMPOUND_BEGIN:
                    return this.compound();
                case Tokens.ARRAY_BEGIN:
                    // Maybe add in a legacy-only mode to read those?
                    if (this.buffer.hasMore(2) && this.buffer.peek(2) == ';') { // we know we're an array tag
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
        } finally {
            this.depth--;
        }
    }

    /**
     * A tag that is definitely some sort of scalar.
     *
     * <p>Does not detect quoted strings, so those should have been parsed already.</p>
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
                        switch (Character.toLowerCase(current)) { // try to read and return as a number
                            case Tokens.TYPE_BYTE:
                                result = new ByteTag(Byte.parseByte(builder.toString()));
                                break;
                            case Tokens.TYPE_SHORT:
                                result = new ShortTag(Short.parseShort(builder.toString()));
                                break;
                            case Tokens.TYPE_INT:
                                result = new IntTag(Integer.parseInt(builder.toString()));
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
            } catch (final NumberFormatException ignored) {
                // Via - don't try to parse as DoubleTag here
            }
        }

        if (built.equalsIgnoreCase(Tokens.LITERAL_TRUE)) {
            return new ByteTag((byte) 1);
        } else if (built.equalsIgnoreCase(Tokens.LITERAL_FALSE)) {
            return new ByteTag((byte) 0);
        }
        return new StringTag(built);

    }

    private boolean separatorOrCompleteWith(final char endCharacter) throws StringTagParseException {
        if (this.buffer.takeIf(endCharacter)) {
            return true;
        }
        this.buffer.expect(Tokens.VALUE_SEPARATOR);
        return this.buffer.takeIf(endCharacter); // Via - trailing commas are allowed
    }

    /**
     * Remove simple escape sequences from a string.
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

    public void legacy(final boolean acceptLegacy) {
        this.acceptLegacy = acceptLegacy;
    }
}
