/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_21to1_20_5.data;

public final class Paintings1_20_5 {

    public static final PaintingVariant[] PAINTINGS = {
        new PaintingVariant("kebab", 1, 1),
        new PaintingVariant("aztec", 1, 1),
        new PaintingVariant("alban", 1, 1),
        new PaintingVariant("aztec2", 1, 1),
        new PaintingVariant("bomb", 1, 1),
        new PaintingVariant("plant", 1, 1),
        new PaintingVariant("wasteland", 1, 1),
        new PaintingVariant("pool", 2, 1),
        new PaintingVariant("courbet", 2, 1),
        new PaintingVariant("sea", 2, 1),
        new PaintingVariant("sunset", 2, 1),
        new PaintingVariant("creebet", 2, 1),
        new PaintingVariant("wanderer", 1, 2),
        new PaintingVariant("graham", 1, 2),
        new PaintingVariant("match", 2, 2),
        new PaintingVariant("bust", 2, 2),
        new PaintingVariant("stage", 2, 2),
        new PaintingVariant("void", 2, 2),
        new PaintingVariant("skull_and_roses", 2, 2),
        new PaintingVariant("wither", 2, 2),
        new PaintingVariant("fighters", 4, 2),
        new PaintingVariant("pointer", 4, 4),
        new PaintingVariant("pigscene", 4, 4),
        new PaintingVariant("burning_skull", 4, 4),
        new PaintingVariant("skeleton", 4, 3),
        new PaintingVariant("earth", 2, 2),
        new PaintingVariant("wind", 2, 2),
        new PaintingVariant("water", 2, 2),
        new PaintingVariant("fire", 2, 2),
        new PaintingVariant("donkey_kong", 4, 3)
    };

    public static final class PaintingVariant {
        private final String key;
        private final int width;
        private final int height;

        public PaintingVariant(final String key, final int width, final int height) {
            this.key = key;
            this.width = width;
            this.height = height;
        }

        public String key() {
            return key;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }
    }
}
