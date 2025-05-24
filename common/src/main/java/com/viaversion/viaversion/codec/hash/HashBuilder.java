/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.codec.hash;

import java.util.Arrays;

public final class HashBuilder {

    private static final byte[] EMPTY_BYTES = new byte[0];
    private final HashFunction hashFunction;
    private byte[] bytes = EMPTY_BYTES;
    private int index;

    public HashBuilder(final HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public HashBuilder writeByte(final byte b) {
        this.ensureSize(1);
        this.bytes[this.index++] = b;
        return this;
    }

    public HashBuilder writeBytes(final byte[] bytes) {
        this.ensureSize(bytes.length);
        System.arraycopy(bytes, 0, this.bytes, this.index, bytes.length);
        this.index += bytes.length;
        return this;
    }

    public void writeBytesDirect(final byte[] bytes) {
        if (this.bytes.length == 0) {
            // Set bytes directly if still empty
            this.bytes = bytes;
            this.index = bytes.length;
        } else {
            writeBytes(bytes);
        }
    }

    public HashBuilder writeBoolean(final boolean value) {
        this.ensureSize(1);
        this.bytes[this.index++] = (byte) (value ? 1 : 0);
        return this;
    }

    public HashBuilder writeShort(final short s) {
        this.ensureSize(2);
        this.bytes[this.index++] = (byte) s;
        this.bytes[this.index++] = (byte) (s >> 8);
        return this;
    }

    public HashBuilder writeChar(final char c) {
        this.ensureSize(2);
        this.bytes[this.index++] = (byte) c;
        this.bytes[this.index++] = (byte) (c >> 8);
        return this;
    }

    public HashBuilder writeString(final CharSequence sequence) {
        this.ensureSize(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            this.writeChar(sequence.charAt(i));
        }
        return this;
    }

    public HashBuilder writeInt(final int i) {
        this.ensureSize(4);
        this.bytes[this.index++] = (byte) i;
        this.bytes[this.index++] = (byte) (i >> 8);
        this.bytes[this.index++] = (byte) (i >> 16);
        this.bytes[this.index++] = (byte) (i >> 24);
        return this;
    }

    public HashBuilder writeLong(final long l) {
        this.ensureSize(8);
        this.bytes[this.index++] = (byte) l;
        this.bytes[this.index++] = (byte) (l >> 8);
        this.bytes[this.index++] = (byte) (l >> 16);
        this.bytes[this.index++] = (byte) (l >> 24);
        this.bytes[this.index++] = (byte) (l >> 32);
        this.bytes[this.index++] = (byte) (l >> 40);
        this.bytes[this.index++] = (byte) (l >> 48);
        this.bytes[this.index++] = (byte) (l >> 56);
        return this;
    }

    public HashBuilder writeFloat(final float f) {
        this.writeInt(Float.floatToIntBits(f));
        return this;
    }

    public HashBuilder writeDouble(final double d) {
        this.writeLong(Double.doubleToLongBits(d));
        return this;
    }

    /**
     * If possible, pre-sizes the writer to ensure it can hold at least the specified number of bytes.
     *
     * @param bytes the minimum size in bytes that the writer should be able to hold
     */
    public HashBuilder preSize(final int bytes) {
        if (this.bytes.length == 0) {
            this.bytes = new byte[bytes];
        } else {
            this.ensureSize(bytes);
        }
        return this;
    }

    private void ensureSize(final int bytes) {
        final int length = this.bytes.length;
        if (this.index + bytes > length) {
            final int newLength = Math.max(length * 2, this.index + bytes);
            this.bytes = Arrays.copyOf(this.bytes, newLength);
        }
    }

    public int hash() {
        return this.hashFunction.hashBytes(this.bytes, this.index);
    }

    public void reset() {
        this.index = 0;
        this.bytes = EMPTY_BYTES;
    }

    public HashFunction function() {
        return hashFunction;
    }
}
