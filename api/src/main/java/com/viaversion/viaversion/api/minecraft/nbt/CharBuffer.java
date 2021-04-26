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

/* package */ final class CharBuffer {
    private final CharSequence sequence;
    private int index;

    CharBuffer(final CharSequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Get the character at the current position
     *
     * @return The current character
     */
    public char peek() {
        return this.sequence.charAt(this.index);
    }

    public char peek(final int offset) {
        return this.sequence.charAt(this.index + offset);
    }

    /**
     * Get the current character and advance
     *
     * @return current character
     */
    public char take() {
        return this.sequence.charAt(this.index++);
    }

    public boolean advance() {
        this.index++;
        return this.hasMore();
    }

    public boolean hasMore() {
        return this.index < this.sequence.length();
    }

    /**
     * Search for the provided token, and advance the reader index past the {@code until} character.
     *
     * @param until Case-insensitive token
     * @return the string starting at the current position (inclusive) and going until the location of {@code until}, exclusive
     */
    public CharSequence takeUntil(char until) throws StringTagParseException {
        until = Character.toLowerCase(until);
        int endIdx = -1;
        for (int idx = this.index; idx < this.sequence.length(); ++idx) {
            if (this.sequence.charAt(idx) == Tokens.ESCAPE_MARKER) {
                idx++;
            } else if (Character.toLowerCase(this.sequence.charAt(idx)) == until) {
                endIdx = idx;
                break;
            }
        }
        if (endIdx == -1) {
            throw this.makeError("No occurrence of " + until + " was found");
        }

        final CharSequence result = this.sequence.subSequence(this.index, endIdx);
        this.index = endIdx + 1;
        return result;
    }

    /**
     * Assert that the next non-whitespace character is the provided parameter.
     *
     * <p>If the assertion is successful, the token will be consumed.</p>
     *
     * @param expectedChar expected character
     * @return this
     * @throws StringTagParseException if EOF or non-matching value is found
     */
    public CharBuffer expect(final char expectedChar) throws StringTagParseException {
        this.skipWhitespace();
        if (!this.hasMore()) {
            throw this.makeError("Expected character '" + expectedChar + "' but got EOF");
        }
        if (this.peek() != expectedChar) {
            throw this.makeError("Expected character '" + expectedChar + "' but got '" + this.peek() + "'");
        }
        this.take();
        return this;
    }

    public CharBuffer skipWhitespace() {
        while (this.hasMore() && Character.isWhitespace(this.peek())) this.advance();
        return this;
    }

    public StringTagParseException makeError(final String message) {
        return new StringTagParseException(message, this.sequence, this.index);
    }
}
