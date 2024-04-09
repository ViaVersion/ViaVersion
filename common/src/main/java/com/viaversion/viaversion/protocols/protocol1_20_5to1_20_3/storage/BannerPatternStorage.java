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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

//TODO Item rewriting doesn't have user connection context yet. That's a fairly disruptive change, so it'll be done later
// Replace BannerPatterns1_20_5.idToKey
public final class BannerPatternStorage implements StorableObject {

    private final Int2ObjectMap<String> bannerPatterns = new Int2ObjectOpenHashMap<>();

    public Int2ObjectMap<String> bannerPatterns() {
        return bannerPatterns;
    }
}
