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
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents an animal variant (e.g. for cows, pigs, chickens).
 *
 * @param modelType model type id
 * @param texture texture path
 * @param biomes biomes this variant can spawn in, if limited to specific biomes
 */
public record AnimalVariant(int modelType, String texture, @Nullable HolderSet biomes) {

    public static HolderType<AnimalVariant> TYPE = new HolderType<>() {
        @Override
        public AnimalVariant readDirect(final ByteBuf buffer) {
            final int modelType = Types.VAR_INT.readPrimitive(buffer);
            final String texture = Types.STRING.read(buffer);
            final HolderSet biomes = Types.OPTIONAL_HOLDER_SET.read(buffer);
            return new AnimalVariant(modelType, texture, biomes);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final AnimalVariant variant) {
            Types.VAR_INT.writePrimitive(buffer, variant.modelType());
            Types.STRING.write(buffer, variant.texture());
            Types.OPTIONAL_HOLDER_SET.write(buffer, variant.biomes());
        }
    };
}
