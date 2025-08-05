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
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;

public class MovementVectorType extends Type<Vector3d> {
    private static final int MAPPED_MAX_VALUE = Short.MAX_VALUE >> 1;
    private static final int SCALE_BITS_MASK = 3; // first 2 bits for the scale
    private static final int CONTINUATION_BIT_MASK = 1 << 2; // 3rd bit to indicate more scale data

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
        final long third = Types.UNSIGNED_INT.read(buffer);
        final long packed = third << 16 | second << 8 | first;

        // 15 bits for each part after removing the scale bits
        final double encodedX = packed >> (0 + SCALE_BITS_MASK) & Short.MAX_VALUE;
        final double encodedY = packed >> (15 + SCALE_BITS_MASK) & Short.MAX_VALUE;
        final double encodedZ = packed >> (30 + SCALE_BITS_MASK) & Short.MAX_VALUE;

        int scale = first & SCALE_BITS_MASK;
        if ((first & CONTINUATION_BIT_MASK) != 0) {
            // Read the remaining bits and add them to the first two
            scale |= Types.VAR_INT.readPrimitive(buffer) << 2;
        }

        return new Vector3d(
            (encodedX / MAPPED_MAX_VALUE - 1) * scale,
            (encodedY / MAPPED_MAX_VALUE - 1) * scale,
            (encodedZ / MAPPED_MAX_VALUE - 1) * scale
        );
    }

    @Override
    public void write(final ByteBuf buffer, final Vector3d vec) {
        final double maxPart = Math.max(Math.abs(vec.x()), Math.max(Math.abs(vec.y()), Math.abs(vec.z())));
        if (maxPart < MathUtil.EPSILON) {
            buffer.writeByte(0);
            return;
        }

        final int scale = (int) Math.ceil(maxPart);
        final double quantizationFactor = 0.5 / scale;
        final long encodedX = (long) ((vec.x() * quantizationFactor + 0.5) * Short.MAX_VALUE);
        final long encodedY = (long) ((vec.y() * quantizationFactor + 0.5) * Short.MAX_VALUE);
        final long encodedZ = (long) ((vec.z() * quantizationFactor + 0.5) * Short.MAX_VALUE);

        final boolean scaleTooLargeForBits = scale > 3;
        final int scaleBits = scaleTooLargeForBits ? (scale & SCALE_BITS_MASK) | CONTINUATION_BIT_MASK : scale;
        final long packed = scaleBits
            | (encodedX << (0 + SCALE_BITS_MASK))
            | (encodedY << (15 + SCALE_BITS_MASK))
            | (encodedZ << (30 + SCALE_BITS_MASK));

        buffer.writeByte((byte) packed);
        buffer.writeByte((byte) (packed >> 8));
        buffer.writeInt((int) (packed >> 16));

        if (scaleTooLargeForBits) {
            // First two bits have already been written
            Types.VAR_INT.writePrimitive(buffer, scale >> 2);
        }
    }
}
