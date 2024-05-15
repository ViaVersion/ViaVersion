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
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public record PaintingVariant(int width, int height, String assetId) {

    public static HolderType<PaintingVariant> TYPE = new HolderType<>() {
        @Override
        public PaintingVariant readDirect(final ByteBuf buffer) {
            final int width = Types.VAR_INT.readPrimitive(buffer);
            final int height = Types.VAR_INT.readPrimitive(buffer);
            final String assetId = Types.STRING.read(buffer);
            return new PaintingVariant(width, height, assetId);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final PaintingVariant variant) {
            Types.VAR_INT.writePrimitive(buffer, variant.width());
            Types.VAR_INT.writePrimitive(buffer, variant.height());
            Types.STRING.write(buffer, variant.assetId());
        }
    };
}
