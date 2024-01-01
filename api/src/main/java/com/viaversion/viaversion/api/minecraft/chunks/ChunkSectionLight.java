/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.chunks;

import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ChunkSectionLight {

    /**
     * Length of the sky and block light nibble arrays.
     */
    int LIGHT_LENGTH = 16 * 16 * 16 / 2; // Dimensions / 2 (nibble bit count)

    /**
     * Returns whether the section has sky light.
     *
     * @return true if skylight is present
     */
    boolean hasSkyLight();

    /**
     * Returns whether the section has block light.
     * This returns true unless specifically set to null.
     *
     * @return true if skylight is present
     */
    boolean hasBlockLight();

    /**
     * Returns the nibblearray's raw sky light byte array if present.
     *
     * @return the nibblearray's raw sky light byte array if present
     * @see #hasSkyLight()
     */
    byte @Nullable [] getSkyLight();

    /**
     * Returns the nibblearray's raw block light byte array if present.
     *
     * @return the nibblearray's raw block light byte array if present
     * @see #hasBlockLight()
     */
    byte @Nullable [] getBlockLight();

    /**
     * Set the sky light array.
     *
     * @param data raw sky light data
     */
    void setSkyLight(byte[] data);

    /**
     * Set the block light array.
     *
     * @param data raw block light data
     */
    void setBlockLight(byte[] data);

    /**
     * Returns the sky light nibblearray.
     *
     * @return sky light nibblearray
     * @see #hasSkyLight()
     */
    @Nullable NibbleArray getSkyLightNibbleArray();

    /**
     * Returns the block light nibblearray.
     *
     * @return block light nibblearray
     * @see #hasBlockLight()
     */
    @Nullable NibbleArray getBlockLightNibbleArray();

    void readSkyLight(ByteBuf input);

    void readBlockLight(ByteBuf input);

    /**
     * Write the sky light to a buffer.
     *
     * @param output buffer to write to
     */
    void writeSkyLight(ByteBuf output);

    /**
     * Write the block light to a buffer.
     *
     * @param output buffer to write to
     */
    void writeBlockLight(ByteBuf output);
}
