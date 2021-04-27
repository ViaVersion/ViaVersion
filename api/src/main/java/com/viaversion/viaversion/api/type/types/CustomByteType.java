/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.viaversion.viaversion.api.type.PartialType;
import io.netty.buffer.ByteBuf;

public class CustomByteType extends PartialType<byte[], Integer> {

    public CustomByteType(Integer param) {
        super(param, byte[].class);
    }

    @Override
    public byte[] read(ByteBuf byteBuf, Integer integer) throws Exception {
        if (byteBuf.readableBytes() < integer) throw new RuntimeException("Readable bytes does not match expected!");

        byte[] byteArray = new byte[integer];
        byteBuf.readBytes(byteArray);

        return byteArray;
    }

    @Override
    public void write(ByteBuf byteBuf, Integer integer, byte[] bytes) throws Exception {
        byteBuf.writeBytes(bytes);
    }
}