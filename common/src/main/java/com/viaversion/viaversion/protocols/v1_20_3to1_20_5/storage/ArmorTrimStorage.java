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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.util.KeyMappings;

public final class ArmorTrimStorage implements StorableObject {

    // Default 1.20.3 registries
    private static final KeyMappings MATERIALS = new KeyMappings(
        "amethyst",
        "copper",
        "diamond",
        "emerald",
        "gold",
        "iron",
        "lapis",
        "netherite",
        "quartz",
        "redstone"
    );
    private static final KeyMappings PATTERNS = new KeyMappings(
        "coast",
        "dune",
        "eye",
        "host",
        "raiser",
        "rib",
        "sentry",
        "shaper",
        "silence",
        "snout",
        "spire",
        "tide",
        "vex",
        "ward",
        "wayfinder",
        "wild"
    );

    private KeyMappings trimPatterns = PATTERNS;
    private KeyMappings trimMaterials = MATERIALS;

    public KeyMappings trimPatterns() {
        return trimPatterns;
    }

    public KeyMappings trimMaterials() {
        return trimMaterials;
    }

    public void setTrimPatterns(final KeyMappings trimPatterns) {
        this.trimPatterns = trimPatterns;
    }

    public void setTrimMaterials(final KeyMappings trimMaterials) {
        this.trimMaterials = trimMaterials;
    }
}
