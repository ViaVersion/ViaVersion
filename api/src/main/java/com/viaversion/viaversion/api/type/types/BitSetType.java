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
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.BitSet;

public class BitSetType extends Type<BitSet> {

    private final int length;
    private final int bytesLength;

    public BitSetType(final int length) {
        super(BitSet.class);
        this.length = length;
        this.bytesLength = (int) Math.ceil(length / 8D);
    }

    @Override
    public BitSet read(ByteBuf buffer) {
        final byte[] bytes = new byte[bytesLength];
        buffer.readBytes(bytes);
        return BitSet.valueOf(bytes);
    }

    @Override
    public void write(ByteBuf buffer, BitSet object) {
        Preconditions.checkArgument(object.length() <= length, "BitSet of length %s larger than max length %s", object.length(), length);
        final byte[] bytes = object.toByteArray();
        buffer.writeBytes(bytes.length == bytesLength ? bytes : Arrays.copyOf(bytes, bytesLength));
    }
}
