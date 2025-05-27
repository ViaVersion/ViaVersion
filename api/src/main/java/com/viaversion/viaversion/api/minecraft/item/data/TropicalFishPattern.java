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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;

// Completely cooked packed id instead of just using an ordinal. Not even consistent enough for FakeEnumType
public record TropicalFishPattern(int packedId) {

    public static final Type<TropicalFishPattern> TYPE = new Type<>(TropicalFishPattern.class) {
        @Override
        public TropicalFishPattern read(final ByteBuf buffer) {
            final int packedId = Types.VAR_INT.readPrimitive(buffer);
            return new TropicalFishPattern(packedId);
        }

        @Override
        public void write(final ByteBuf buffer, final TropicalFishPattern value) {
            Types.VAR_INT.writePrimitive(buffer, value.packedId);
        }

        @Override
        public void write(final Ops ops, final TropicalFishPattern value) {
            final Pattern pattern = Arrays.stream(Pattern.values()).filter(e -> e.packedId == value.packedId).findAny().orElse(Pattern.KOB);
            ops.write(Types.STRING, pattern.key);
        }
    };

    public int sizeId() {
        return packedId & 0xFF;
    }

    public int sizeSpecificId() {
        return packedId >> 8;
    }

    private enum Pattern {
        KOB("kob", 0, 0),
        SUNSTREAK("sunstreak", 0, 1),
        SNOOPER("snooper", 0, 2),
        DASHER("dasher", 0, 3),
        BRINELY("brinely", 0, 4),
        SPOTTY("spotty", 0, 5),
        FLOPPER("flopper", 1, 0),
        STRIPEY("stripey", 1, 1),
        GLITTER("glitter", 1, 2),
        BLOCKFISH("blockfish", 1, 3),
        BETTY("betty", 1, 4),
        CLAYFISH("clayfish", 1, 5);

        private final String key;
        private final int packedId;

        Pattern(final String key, final int sizeId, final int sizeSpecificId) {
            this.key = key;
            this.packedId = sizeId | (sizeSpecificId << 8);
        }
    }
}
