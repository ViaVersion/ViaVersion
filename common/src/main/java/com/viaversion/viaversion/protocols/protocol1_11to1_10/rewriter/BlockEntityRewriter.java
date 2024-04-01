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
package com.viaversion.viaversion.protocols.protocol1_11to1_10.rewriter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.util.Key;

public class BlockEntityRewriter {
    private static final BiMap<String, String> OLD_TO_NEW_NAMES = HashBiMap.create();

    // Source: https://www.minecraftforum.net/forums/minecraft-java-edition/redstone-discussion-and/commands-command-blocks-and/2724507-1-11-nbt-changes-and-additions#AllTiles
    static {
        rewrite("Furnace", "furnace");
        rewrite("Chest", "chest");
        rewrite("EnderChest", "ender_chest");
        rewrite("RecordPlayer", "jukebox");
        rewrite("Trap", "dispenser");
        rewrite("Dropper", "dropper");
        rewrite("Sign", "sign");
        rewrite("MobSpawner", "mob_spawner");
        rewrite("Music", "noteblock");
        rewrite("Piston", "piston");
        rewrite("Cauldron", "brewing_stand");
        rewrite("EnchantTable", "enchanting_table");
        rewrite("Airportal", "end_portal");
        rewrite("Beacon", "beacon");
        rewrite("Skull", "skull");
        rewrite("DLDetector", "daylight_detector");
        rewrite("Hopper", "hopper");
        rewrite("Comparator", "comparator");
        rewrite("FlowerPot", "flower_pot");
        rewrite("Banner", "banner");
        rewrite("Structure", "structure_block");
        rewrite("EndGateway", "end_gateway");
        rewrite("Control", "command_block");
    }

    private static void rewrite(String oldName, String newName) {
        OLD_TO_NEW_NAMES.put(oldName, Key.namespaced(newName));
    }

    public static BiMap<String, String> inverse() {
        return OLD_TO_NEW_NAMES.inverse();
    }

    public static String toNewIdentifier(String oldId) {
        String newName = OLD_TO_NEW_NAMES.get(oldId);
        if (newName != null) {
            return newName;
        }
        return oldId;
    }
}
