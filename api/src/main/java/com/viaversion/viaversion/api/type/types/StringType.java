/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class StringType extends Type<String> {
    // String#length() (used to limit the string in Minecraft source code) uses char[]#length
    private static final int MAX_CHAR_UTF_8_LENGTH = Character.toString(Character.MAX_VALUE)
        .getBytes(StandardCharsets.UTF_8).length;
    private final int maxLength;

    public StringType() {
        this(Short.MAX_VALUE);
    }

    public StringType(int maxLength) {
        super(String.class);
        this.maxLength = maxLength;
    }

    @Override
    public String read(ByteBuf buffer) {
        int len = Types.VAR_INT.readPrimitive(buffer);

        Preconditions.checkArgument(len <= maxLength * MAX_CHAR_UTF_8_LENGTH,
            "Cannot receive string longer than " + maxLength + " * " + MAX_CHAR_UTF_8_LENGTH + " bytes (got %s bytes)", len);

        String string = buffer.toString(buffer.readerIndex(), len, StandardCharsets.UTF_8);
        buffer.skipBytes(len);

        Preconditions.checkArgument(string.length() <= maxLength,
            "Cannot receive string longer than " + maxLength + " characters (got %s bytes)", string.length());

        return string;
    }

    @Override
    public void write(ByteBuf buffer, String object) {
        if (object.length() > maxLength) {
            throw new IllegalArgumentException("Cannot send string longer than " + maxLength + " characters (got " + object.length() + " characters)");
        }

        byte[] b = object.getBytes(StandardCharsets.UTF_8);
        Types.VAR_INT.writePrimitive(buffer, b.length);
        buffer.writeBytes(b);
    }

    @Override
    public void write(final Ops ops, final String value) {
        ops.writeString(value);
    }

    public static final class OptionalStringType extends OptionalType<String> {

        public OptionalStringType() {
            super(Types.STRING);
        }
    }
}
