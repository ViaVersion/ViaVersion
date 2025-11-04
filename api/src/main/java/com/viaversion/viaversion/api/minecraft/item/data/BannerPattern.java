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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public record BannerPattern(String assetId, String translationKey) {

    public static final HolderType<BannerPattern> TYPE = new HolderType<>() {
        @Override
        public BannerPattern readDirect(final ByteBuf buffer) {
            final String assetId = Types.STRING.read(buffer);
            final String translationKey = Types.STRING.read(buffer);
            return new BannerPattern(assetId, translationKey);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final BannerPattern value) {
            Types.STRING.write(buffer, value.assetId);
            Types.STRING.write(buffer, value.translationKey);
        }

        @Override
        public void writeDirect(final Ops ops, final BannerPattern object) {
            ops.writeMap(map -> map
                .write("asset_id", Types.IDENTIFIER, Key.of(object.assetId))
                .write("translation_key", Types.STRING, object.translationKey));
        }

        @Override
        protected Key identifier(final Ops ops, final int id) {
            return ops.context().registryAccess().registryKey("banner_pattern", id);
        }
    };
}
