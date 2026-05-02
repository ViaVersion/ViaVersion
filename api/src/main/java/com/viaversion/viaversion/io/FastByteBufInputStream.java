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
import io.netty.buffer.ByteBufInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;

/**
 * {@link ByteBufInputStream} alternative with a more fine-tuned {@link #readUTF()} implementation
 * for fast reading of NBT strings.
 */
public final class FastByteBufInputStream extends InputStream implements DataInput {

    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final char[] EMPTY_CHARS = new char[0];
    private byte[] bytearr = EMPTY_BYTES;
    private char[] chararr = EMPTY_CHARS;
    private final ByteBuf buf;

    public FastByteBufInputStream(final ByteBuf buffer) {
        Preconditions.checkNotNull(buffer);
        this.buf = buffer;
        buffer.markReaderIndex();
    }

    @Override
    public String readUTF() throws IOException {
        // Note that this is a modified UTF-8 format, so we can't use netty's string reading
        final int utflen = readUnsignedShort();
        if (utflen == 0) {
            return "";
        }

        final ByteBuf buf = this.buf;
        final int readerIdx = buf.readerIndex();
        if (buf.readableBytes() < utflen) {
            throw new EOFException();
        }

        final String result;
        if (buf.hasArray()) {
            // Directly pass in the backing array
            result = decodeFromArray(buf.array(), buf.arrayOffset() + readerIdx, utflen);
            buf.readerIndex(readerIdx + utflen);
        } else {
            // We need to construct some array for the String anyway, so reading it into an array is fine;
            // share the byte array.
            final byte[] data = ensureBytes(utflen);
            buf.readBytes(data, 0, utflen);
            result = decodeFromArray(data, 0, utflen);
        }
        return result;
    }

    private String decodeFromArray(final byte[] data, final int offset, final int utflen) throws UTFDataFormatException {
        // Go through ASCII symbols and directly pass through as a latin-1 string if possible
        int i = 0;
        while (i < utflen && data[offset + i] >= 0) {
            i++;
        }
        if (i == utflen) {
            return new String(data, offset, utflen, StandardCharsets.ISO_8859_1);
        }

        // Continue with a regular UTF-8 char decode
        final char[] chars = ensureChars(utflen);
        for (int j = 0; j < i; j++) {
            chars[j] = (char) data[offset + j];
        }
        final int charCount = decode(data, offset + i, offset + utflen, chars, i);
        return new String(chars, 0, charCount);
    }

    private static int decode(final byte[] data, int pos, final int end, final char[] chars, int charCount) throws UTFDataFormatException {
        // From DataInputStream
        while (pos < end) {
            final int c = data[pos] & 0xff;
            switch (c >> 4) {
                case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                    pos++;
                    chars[charCount++] = (char) c;
                }
                case 12, 13 -> {
                    if (pos + 2 > end) {
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    }
                    final int char2 = data[pos + 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("malformed input around byte " + (pos + 2));
                    }
                    chars[charCount++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    pos += 2;
                }
                case 14 -> {
                    if (pos + 3 > end) {
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    }
                    final int char2 = data[pos + 1];
                    final int char3 = data[pos + 2];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException("malformed input around byte " + (pos + 2));
                    }
                    chars[charCount++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
                    pos += 3;
                }
                default -> throw new UTFDataFormatException("malformed input around byte " + pos);
            }
        }
        return charCount;
    }

    private byte[] ensureBytes(final int len) {
        if (bytearr.length < len) {
            bytearr = new byte[Math.max(len, 64)];
        }
        return bytearr;
    }

    private char[] ensureChars(final int len) {
        if (chararr.length < len) {
            chararr = new char[Math.max(len, 64)];
        }
        return chararr;
    }

    @Override
    public int available() {
        return buf.readableBytes();
    }

    @Override
    public void mark(final int readlimit) {
        buf.markReaderIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!buf.isReadable()) {
            return -1;
        }
        return buf.readByte() & 0xff;
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        final int available = buf.readableBytes();
        if (available == 0) {
            return -1;
        }
        len = Math.min(available, len);
        buf.readBytes(b, off, len);
        return len;
    }

    @Override
    public void reset() throws IOException {
        buf.resetReaderIndex();
    }

    @Override
    public long skip(final long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        checkAvailable(1);
        return buf.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        checkAvailable(1);
        return buf.readByte();
    }

    @Override
    public char readChar() {
        return (char) buf.readShort();
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(buf.readLong());
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(buf.readInt());
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
        }
        checkAvailable(len);
        buf.readBytes(b, off, len);
    }

    @Override
    public int readInt() throws IOException {
        checkAvailable(4);
        return buf.readInt();
    }

    @Override
    public String readLine() {
        // Don't reuse the StringBuilder here, this is not relevant for packet reading
        final StringBuilder lineBuf = new StringBuilder();
        int available = available();
        if (available == 0) {
            return null;
        }
        loop:
        do {
            int c = buf.readUnsignedByte();
            --available;
            switch (c) {
                case '\n':
                    break loop;

                case '\r':
                    if (available > 0 && (char) buf.getUnsignedByte(buf.readerIndex()) == '\n') {
                        buf.skipBytes(1);
                        --available;
                    }
                    break loop;

                default:
                    lineBuf.append((char) c);
            }
        } while (available > 0);
        return !lineBuf.isEmpty() ? lineBuf.toString() : "";
    }

    @Override
    public long readLong() throws IOException {
        checkAvailable(8);
        return buf.readLong();
    }

    @Override
    public short readShort() throws IOException {
        checkAvailable(2);
        return buf.readShort();
    }

    @Override
    public int readUnsignedByte() {
        return buf.readByte() & 0xff;
    }

    @Override
    public int readUnsignedShort() {
        return buf.readShort() & 0xffff;
    }

    @Override
    public int skipBytes(final int n) {
        final int nBytes = Math.min(available(), n);
        buf.skipBytes(nBytes);
        return nBytes;
    }

    private void checkAvailable(final int fieldSize) throws IOException {
        if (fieldSize > buf.readableBytes()) {
            throw new EOFException("fieldSize is too long! Length is " + fieldSize + ", but maximum is " + available());
        }
    }
}
