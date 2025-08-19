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
package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.UnsignedIntType;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;

public class MovementVectorType extends Type<Vector3d> {
    // Using 15 bits, steal short constants for it
    private static final double MAX_QUANTIZED_VALUE = Short.MAX_VALUE - 1;
    private static final int SCALE_BITS = 2; // first 2 bits for the scale
    private static final int SCALE_BITS_MASK = 3;
    private static final int CONTINUATION_FLAG = 1 << 2; // 3rd bit to indicate more scale data
    private static final double ABS_MAX_VALUE = (1L << Integer.SIZE + SCALE_BITS) - 1; // Unsigned int + the first two scale bits
    private static final double ABS_MIN_VALUE = 1 / MAX_QUANTIZED_VALUE;

    public MovementVectorType() {
        super(Vector3d.class);
    }

    @Override
    public Vector3d read(final ByteBuf buffer) {
        // Pack values back into a single long
        final int first = Types.UNSIGNED_BYTE.read(buffer);
        if (first == 0) {
            return Vector3d.ZERO;
        }

        final int second = Types.UNSIGNED_BYTE.read(buffer);
        final long remaining = Types.UNSIGNED_INT.read(buffer);
        final long packed = remaining << 16 | second << 8 | first;

        long scale = first & SCALE_BITS_MASK;
        if ((first & CONTINUATION_FLAG) != 0) {
            // Read the remaining bits and add them to the first two
            scale |= (Types.VAR_INT.readPrimitive(buffer) & UnsignedIntType.MAX_UNSIGNED_INT) << SCALE_BITS;
        }

        // 15 bits for each part after removing the scale bits
        return new Vector3d(
            unpack(packed >> (0 + SCALE_BITS_MASK)) * scale,
            unpack(packed >> (15 + SCALE_BITS_MASK)) * scale,
            unpack(packed >> (30 + SCALE_BITS_MASK)) * scale
        );
    }

    @Override
    public void write(final ByteBuf buffer, final Vector3d vec) {
        final double x = sanitize(vec.x());
        final double y = sanitize(vec.y());
        final double z = sanitize(vec.z());

        final double maxPart = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
        if (maxPart < ABS_MIN_VALUE) {
            buffer.writeByte(0);
            return;
        }

        final long scale = MathUtil.ceilLong(maxPart);
        final boolean scaleTooLargeForBits = (scale & SCALE_BITS_MASK) != scale;
        final long scaleBits = scaleTooLargeForBits ? (scale & SCALE_BITS_MASK) | CONTINUATION_FLAG : scale;
        final long packed = scaleBits
            | (pack(x / scale) << (0 + SCALE_BITS_MASK))
            | (pack(y / scale) << (15 + SCALE_BITS_MASK))
            | (pack(z / scale) << (30 + SCALE_BITS_MASK));

        buffer.writeByte((byte) packed);
        buffer.writeByte((byte) (packed >> 8));
        buffer.writeInt((int) (packed >> 16));

        if (scaleTooLargeForBits) {
            // First two bits have already been written
            Types.VAR_INT.writePrimitive(buffer, (int) (scale >> SCALE_BITS));
        }
    }

    private double sanitize(final double value) {
        return Double.isNaN(value) ? 0 : MathUtil.clamp(value, -ABS_MAX_VALUE, ABS_MAX_VALUE);
    }

    private long pack(final double value) {
        // Shift to [0, 1] and quantize to 15 bits
        final double shifted = (value * 0.5) + 0.5;
        return Math.round(shifted * MAX_QUANTIZED_VALUE);
    }

    private double unpack(final long value) {
        // Normalize to [-1, 1]
        final double clamped = Math.min(value & Short.MAX_VALUE, MAX_QUANTIZED_VALUE);
        return (clamped * 2) / MAX_QUANTIZED_VALUE - 1;
    }
}
