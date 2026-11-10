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
package com.viaversion.viaversion.io;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

/**
 * {@link ByteBufOutputStream} alternative with a more fine-tuned {@link #writeUTF(String)} implementation
 * for fast writing of NBT strings.
 */
public final class FastByteBufOutputStream extends OutputStream implements DataOutput {

    private final ByteBuf buf;

    public FastByteBufOutputStream(final ByteBuf buffer) {
        Preconditions.checkNotNull(buffer);
        this.buf = buffer;
    }

    @Override
    public void writeUTF(final String s) throws IOException {
        // This writes the individual characters to the buffer without an intermediary array.
        // What's lost from no longer bulk writing is gained from avoiding allocation and multiple
        // extra iterations of the full string.
        final int len = s.length();
        if (len == 0) {
            buf.writeShort(0);
            return;
        }
        if (len > 65535) {
            throw new UTFDataFormatException("encoded string too long: " + len + " characters");
        }

        // Reserve space for the length prefix
        final int lenIndex = buf.writerIndex();
        final int startIndex = lenIndex + Short.BYTES;
        buf.ensureWritable(Short.BYTES + len); // even if it ends up being larger, netty does a good job at avoiding constant resizing
        buf.writeShort(len);

        // Start writing single ASCII bytes
        int i = 0;
        for (; i < len; i++) {
            final char c = s.charAt(i);
            if (c >= 0x80 || c == 0) break;
            buf.writeByte(c);
        }

        if (i == len) {
            return; // done
        }

        // Write the rest
        for (; i < len; i++) {
            final char c = s.charAt(i);
            if (c < 0x80 && c != 0) {
                buf.writeByte(c);
            } else if (c >= 0x800) {
                buf.writeByte(0xE0 | ((c >> 12) & 0x0F));
                buf.writeByte(0x80 | ((c >> 6) & 0x3F));
                buf.writeByte(0x80 | (c & 0x3F));
            } else {
                buf.writeByte(0xC0 | ((c >> 6) & 0x1F));
                buf.writeByte(0x80 | (c & 0x3F));
            }
        }

        // Don't do another round of counting just for the bounds check; check at the end,
        // even if that means that in the worst case this might have already written a good bit over the threshold.
        final int utflen = buf.writerIndex() - startIndex;
        if (utflen > 65535) {
            buf.writerIndex(lenIndex);
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
        }

        // Set the actual length
        buf.setShort(lenIndex, utflen);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        buf.writeBytes(b, off, len);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        buf.writeBytes(b);
    }

    @Override
    public void write(final int b) throws IOException {
        buf.writeByte(b);
    }

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        buf.writeBoolean(v);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        buf.writeByte(v);
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        final int length = s.length();
        buf.ensureWritable(length);
        final int offset = buf.writerIndex();
        for (int i = 0; i < length; i++) {
            buf.setByte(offset + i, (byte) s.charAt(i));
        }
        buf.writerIndex(offset + length);
    }

    @Override
    public void writeChar(final int v) {
        buf.writeChar(v);
    }

    @Override
    public void writeChars(final String s) {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            buf.writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        buf.writeDouble(v);
    }

    @Override
    public void writeFloat(final float v) throws IOException {
        buf.writeFloat(v);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        buf.writeInt(v);
    }

    @Override
    public void writeLong(final long v) throws IOException {
        buf.writeLong(v);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        buf.writeShort((short) v);
    }
}
