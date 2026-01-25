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
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PaintingVariant(int width, int height, String assetId, @Nullable Tag title, @Nullable Tag author) {

    public PaintingVariant(final int width, final int height, final String assetId) {
        this(width, height, assetId, null, null);
    }

    public static HolderType<PaintingVariant> TYPE1_21 = new HolderType<>() {
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
    public static HolderType<PaintingVariant> TYPE1_21_2 = new HolderType<>() {
        @Override
        public PaintingVariant readDirect(final ByteBuf buffer) {
            final int width = Types.VAR_INT.readPrimitive(buffer);
            final int height = Types.VAR_INT.readPrimitive(buffer);
            final String assetId = Types.STRING.read(buffer);
            final Tag title = Types.TRUSTED_OPTIONAL_TAG.read(buffer);
            final Tag author = Types.TRUSTED_OPTIONAL_TAG.read(buffer);
            return new PaintingVariant(width, height, assetId, title, author);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final PaintingVariant variant) {
            Types.VAR_INT.writePrimitive(buffer, variant.width());
            Types.VAR_INT.writePrimitive(buffer, variant.height());
            Types.STRING.write(buffer, variant.assetId());
            Types.TRUSTED_OPTIONAL_TAG.write(buffer, variant.title());
            Types.TRUSTED_OPTIONAL_TAG.write(buffer, variant.author());
        }

        @Override
        public void writeDirect(final Ops ops, final PaintingVariant value) {
            // Unused, cannot be direct despite having a direct network codec
            ops.writeMap(map -> map
                .write("width", Types.INT, value.width())
                .write("height", Types.INT, value.height())
                .write("asset_id", Types.IDENTIFIER, Key.of(value.assetId()))
                .writeOptional("title", Types.TRUSTED_TAG, value.title())
                .writeOptional("author", Types.TRUSTED_TAG, value.author()));
        }

        @Override
        protected Key identifier(final Ops ops, final int id) {
            return ops.context().registryAccess().registryKey("painting_variant", id);
        }
    };
}
