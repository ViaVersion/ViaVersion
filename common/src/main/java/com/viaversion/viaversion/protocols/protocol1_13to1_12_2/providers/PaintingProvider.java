/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers;

import com.viaversion.viaversion.api.platform.providers.Provider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PaintingProvider implements Provider {
    private final Map<String, Integer> paintings = new HashMap<>();

    public PaintingProvider() {
        add("kebab");
        add("aztec");
        add("alban");
        add("aztec2");
        add("bomb");
        add("plant");
        add("wasteland");
        add("pool");
        add("courbet");
        add("sea");
        add("sunset");
        add("creebet");
        add("wanderer");
        add("graham");
        add("match");
        add("bust");
        add("stage");
        add("void");
        add("skullandroses");
        add("wither");
        add("fighters");
        add("pointer");
        add("pigscene");
        add("burningskull");
        add("skeleton");
        add("donkeykong");
    }

    private void add(String motive) {
        paintings.put("minecraft:" + motive, paintings.size());
    }

    public Optional<Integer> getIntByIdentifier(String motive) {
        // Handle older versions
        if (!motive.startsWith("minecraft:"))
            motive = "minecraft:" + motive.toLowerCase(Locale.ROOT);
        return Optional.ofNullable(paintings.get(motive));
    }
}