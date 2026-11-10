/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_8to1_9.data;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;

public class PotionIdMappings1_9 {

    public static final Map<String, Integer> POTION_NAME_TO_ID = new HashMap<>();
    public static final Map<Integer, String> POTION_ID_TO_NAME = new HashMap<>();

    public static final Int2IntMap POTION_INDEX = new Int2IntOpenHashMap(36);

    static {
        register(-1, "empty");
        register(0, "water");
        register(64, "mundane");
        register(32, "thick");
        register(16, "awkward");

        register(8198, "night_vision");
        register(8262, "long_night_vision");

        register(8206, "invisibility");
        register(8270, "long_invisibility");

        register(8203, "leaping");
        register(8267, "long_leaping");
        register(8235, "strong_leaping");

        register(8195, "fire_resistance");
        register(8259, "long_fire_resistance");

        register(8194, "swiftness");
        register(8258, "long_swiftness");
        register(8226, "strong_swiftness");

        register(8202, "slowness");
        register(8266, "long_slowness");

        register(8205, "water_breathing");
        register(8269, "long_water_breathing");

        register(8261, "healing");
        register(8229, "strong_healing");

        register(8204, "harming");
        register(8236, "strong_harming");

        register(8196, "poison");
        register(8260, "long_poison");
        register(8228, "strong_poison");

        register(8193, "regeneration");
        register(8257, "long_regeneration");
        register(8225, "strong_regeneration");

        register(8201, "strength");
        register(8265, "long_strength");
        register(8233, "strong_strength");

        register(8200, "weakness");
        register(8264, "long_weakness");

    }

    public static String potionNameFromDamage(short damage) {
        String cached = POTION_ID_TO_NAME.get((int) damage);
        if (cached != null) {
            return cached;
        }
        if (damage == 0) {
            return "water";
        }

        int effect = damage & 0xF;
        int name = damage & 0x3F;
        boolean enhanced = (damage & 0x20) > 0;
        boolean extended = (damage & 0x40) > 0;

        boolean canEnhance = true;
        boolean canExtend = true;

        String id;
        switch (effect) {
            case 1 -> id = "regeneration";
            case 2 -> id = "swiftness";
            case 3 -> {
                id = "fire_resistance";
                canEnhance = false;
            }
            case 4 -> id = "poison";
            case 5 -> {
                id = "healing";
                canExtend = false;
            }
            case 6 -> {
                id = "night_vision";
                canEnhance = false;
            }
            case 8 -> {
                id = "weakness";
                canEnhance = false;
            }
            case 9 -> id = "strength";
            case 10 -> {
                id = "slowness";
                canEnhance = false;
            }
            case 11 -> id = "leaping";
            case 12 -> {
                id = "harming";
                canExtend = false;
            }
            case 13 -> {
                id = "water_breathing";
                canEnhance = false;
            }
            case 14 -> {
                id = "invisibility";
                canEnhance = false;
            }
            default -> {
                canEnhance = false;
                canExtend = false;
                id = switch (name) {
                    case 0 -> "mundane";
                    case 16 -> "awkward";
                    case 32 -> "thick";
                    default -> "empty";
                };
            }
        }

        if (effect > 0) {
            if (canEnhance && enhanced) {
                id = "strong_" + id;
            } else if (canExtend && extended) {
                id = "long_" + id;
            }
        }

        return id;
    }

    public static int getNewPotionID(int oldID) {
        if (oldID >= 16384) {
            oldID -= 8192;
        }

        int index = POTION_INDEX.get(oldID);
        if (index != -1) {
            return index;
        }

        oldID = POTION_NAME_TO_ID.get(potionNameFromDamage((short) oldID));
        return (index = POTION_INDEX.get(oldID)) != -1 ? index : 0;
    }

    private static void register(final int id, final String name) {
        POTION_INDEX.put(id, POTION_ID_TO_NAME.size());
        POTION_ID_TO_NAME.put(id, name);
        POTION_NAME_TO_ID.put(name, id);
    }
}
