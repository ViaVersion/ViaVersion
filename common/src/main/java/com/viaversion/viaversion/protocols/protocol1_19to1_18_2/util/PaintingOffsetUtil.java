/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.util;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.Position3d;

public final class PaintingOffsetUtil {

    private static final double MAGIC_OFFSET = -0.46875;
    private static final PaintingVariant[] VARIANTS = {
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(16, 16),
            new PaintingVariant(32, 16),
            new PaintingVariant(32, 16),
            new PaintingVariant(32, 16),
            new PaintingVariant(32, 16),
            new PaintingVariant(32, 16),
            new PaintingVariant(16, 32),
            new PaintingVariant(16, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(32, 32),
            new PaintingVariant(64, 32),
            new PaintingVariant(64, 64),
            new PaintingVariant(64, 64),
            new PaintingVariant(64, 64),
            new PaintingVariant(64, 48),
            new PaintingVariant(64, 48)
    };

    public static Position3d fixOffset(final Position position, final int motive, final int direction) {
        final PaintingVariant variant = VARIANTS[motive];
        final double offY = variant.height > 1 && variant.height != 3 ? 0.5 : 0;
        final double offX;
        final double offZ;
        final double widthOffset = variant.width > 1 ? 0.5 : 0;
        switch (direction) {
            case 0:
                offX = widthOffset;
                offZ = MAGIC_OFFSET;
                break;
            case 1:
                offX = -MAGIC_OFFSET;
                offZ = widthOffset;
                break;
            case 2:
                offX = -widthOffset;
                offZ = -MAGIC_OFFSET;
                break;
            case 3:
                offX = MAGIC_OFFSET;
                offZ = -widthOffset;
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        return new Position3d(position.x() + offX + 0.5d, position.y() + offY + 0.5d, position.z() + offZ + 0.5d);
    }

    private static final class PaintingVariant {
        private final int width;
        private final int height;

        private PaintingVariant(final int width, final int height) {
            this.width = width / 16;
            this.height = height / 16;
        }
    }
}
