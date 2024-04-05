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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.util;

import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PotionEffects1_20_2 {

    private static final Object2IntMap<String> KEY_TO_ID = new Object2IntOpenHashMap<>();
    private static final String[] POTION_EFFECTS = {
            "speed",
            "slowness",
            "haste",
            "mining_fatigue",
            "strength",
            "instant_health",
            "instant_damage",
            "jump_boost",
            "nausea",
            "regeneration",
            "resistance",
            "fire_resistance",
            "water_breathing",
            "invisibility",
            "blindness",
            "night_vision",
            "hunger",
            "weakness",
            "poison",
            "wither",
            "health_boost",
            "absorption",
            "saturation",
            "glowing",
            "levitation",
            "luck",
            "unluck",
            "slow_falling",
            "conduit_power",
            "dolphins_grace",
            "bad_omen",
            "hero_of_the_village",
            "darkness"
    };

    static {
        for (int i = 0; i < POTION_EFFECTS.length; i++) {
            final String effect = POTION_EFFECTS[i];
            KEY_TO_ID.put(effect, i);
        }
        KEY_TO_ID.defaultReturnValue(-1);
    }

    public static @Nullable String idToKey(final int id) {
        return id >= 0 && id < POTION_EFFECTS.length ? Key.namespaced(POTION_EFFECTS[id]) : null;
    }

    public static String idToKeyOrLuck(final int id) {
        return id >= 0 && id < POTION_EFFECTS.length ? Key.namespaced(POTION_EFFECTS[id]) : "minecraft:luck";
    }

    public static int keyToId(final String key) {
        return KEY_TO_ID.getInt(Key.stripMinecraftNamespace(key));
    }
}
