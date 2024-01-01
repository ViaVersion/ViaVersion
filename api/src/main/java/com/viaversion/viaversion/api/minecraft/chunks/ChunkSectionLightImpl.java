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

public class ChunkSectionLightImpl implements ChunkSectionLight {

    private NibbleArray blockLight;
    private NibbleArray skyLight;

    public ChunkSectionLightImpl() {
        // Block light is always written
        this.blockLight = new NibbleArray(ChunkSection.SIZE);
    }

    @Override
    public void setBlockLight(byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(data);
        } else {
            this.blockLight.setHandle(data);
        }
    }

    @Override
    public void setSkyLight(byte[] data) {
        if (data == null) {
            this.skyLight = null;
            return;
        }

        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(data);
        } else {
            this.skyLight.setHandle(data);
        }
    }

    @Override
    public byte @Nullable [] getBlockLight() {
        return blockLight == null ? null : blockLight.getHandle();
    }

    @Override
    public @Nullable NibbleArray getBlockLightNibbleArray() {
        return blockLight;
    }

    @Override
    public byte @Nullable [] getSkyLight() {
        return skyLight == null ? null : skyLight.getHandle();
    }

    @Override
    public @Nullable NibbleArray getSkyLightNibbleArray() {
        return skyLight;
    }

    @Override
    public void readBlockLight(ByteBuf input) {
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.blockLight.getHandle());
    }

    @Override
    public void readSkyLight(ByteBuf input) {
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.skyLight.getHandle());
    }

    @Override
    public void writeBlockLight(ByteBuf output) {
        output.writeBytes(blockLight.getHandle());
    }

    @Override
    public void writeSkyLight(ByteBuf output) {
        output.writeBytes(skyLight.getHandle());
    }

    @Override
    public boolean hasSkyLight() {
        return skyLight != null;
    }

    @Override
    public boolean hasBlockLight() {
        return blockLight != null;
    }
}
