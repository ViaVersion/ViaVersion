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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class Effect {

    private static final Int2IntMap EFFECTS = new Int2IntOpenHashMap(19, .99F);

    static {
        addRewrite(1005, 1010); //Play music disc
        addRewrite(1003, 1005); //Iron door open
        addRewrite(1006, 1011); //Iron door close
        addRewrite(1004, 1009); //Fizz / Fire extinguished
        addRewrite(1007, 1015); //Ghast charge / warns
        addRewrite(1008, 1016); //Ghast shoot
        addRewrite(1009, 1016); //Ghast shoot (Lower volume according to wiki.vg)
        addRewrite(1010, 1019); //Zombie attacks wood door
        addRewrite(1011, 1020); //Zombie attacks metal door
        addRewrite(1012, 1021); //Zombie breaks  wood door
        addRewrite(1014, 1024); //Wither shoot
        addRewrite(1015, 1025); //Bat takeoff / aka herobrine
        addRewrite(1016, 1026); //Zombie inject
        addRewrite(1017, 1027); //Zombie villager converted
        addRewrite(1020, 1029); //Anvil break
        addRewrite(1021, 1030); //Anvil use
        addRewrite(1022, 1031); //Anvil land
        addRewrite(1013, 1023); //Wither spawn
        addRewrite(1018, 1028); //EnderDragon end
    }

    public static int getNewId(int id) {
        return EFFECTS.getOrDefault(id, id);
    }

    public static boolean contains(int oldId) {
        return EFFECTS.containsKey(oldId);
    }

    private static void addRewrite(int oldId, int newId) {
        EFFECTS.put(oldId, newId);
    }
}
