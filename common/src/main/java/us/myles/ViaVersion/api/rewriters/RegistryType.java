/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.rewriters;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum RegistryType {

    BLOCK("block"),
    ITEM("item"),
    FLUID("fluid"),
    ENTITY("entity_type"),
    GAME_EVENT("game_event");

    private static final Map<String, RegistryType> MAP = new HashMap<>();
    private static final RegistryType[] VALUES = values();

    static {
        for (RegistryType type : getValues()) {
            MAP.put(type.resourceLocation, type);
        }
    }

    public static RegistryType[] getValues() {
        return VALUES;
    }

    @Nullable
    public static RegistryType getByKey(String resourceKey) {
        return MAP.get(resourceKey);
    }

    private final String resourceLocation;

    RegistryType(final String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }
}
