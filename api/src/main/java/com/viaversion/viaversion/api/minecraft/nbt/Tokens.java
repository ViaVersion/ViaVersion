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

/**
 * See https://github.com/KyoriPowered/adventure.
 */
/* package */ final class Tokens {
    // Compounds
    static final char COMPOUND_BEGIN = '{';
    static final char COMPOUND_END = '}';
    static final char COMPOUND_KEY_TERMINATOR = ':';

    // Arrays
    static final char ARRAY_BEGIN = '[';
    static final char ARRAY_END = ']';
    static final char ARRAY_SIGNATURE_SEPARATOR = ';';

    static final char VALUE_SEPARATOR = ',';

    static final char SINGLE_QUOTE = '\'';
    static final char DOUBLE_QUOTE = '"';
    static final char ESCAPE_MARKER = '\\';

    static final char TYPE_BYTE = 'B';
    static final char TYPE_SHORT = 'S';
    static final char TYPE_INT = 'I'; // array only
    static final char TYPE_LONG = 'L';
    static final char TYPE_FLOAT = 'F';
    static final char TYPE_DOUBLE = 'D';

    static final char EOF = '\0';

    private Tokens() {
    }

    /**
     * Return if a character is a valid component in an identifier
     *
     * <p>An identifier character must match the expression {@code [a-zA-Z0-9_+.-]}</p>
     *
     * @param c the character
     * @return identifier
     */
    static boolean id(final char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '-' || c == '_'
                || c == '.' || c == '+';
    }

    /**
     * Return whether a character could be at some position in a number.
     * <p>
     * A string passing this check does not necessarily mean it is syntactically valid
     *
     * @param c character to check
     * @return if possibly part of a number
     */
    static boolean numeric(final char c) {
        return (c >= '0' && c <= '9') // digit
                || c == '+' || c == '-' // positive or negative
                || c == 'e' || c == 'E' // exponent
                || c == '.'; // decimal
    }
}
