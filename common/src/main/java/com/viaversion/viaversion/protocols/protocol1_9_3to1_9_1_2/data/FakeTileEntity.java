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
package com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Mojang changed the way how tile entities inside chunk packets work in 1.9.3/4
 * It requires now to have all tile entity data included in the chunk packet, otherwise it'll crash.
 */
public class FakeTileEntity {
    private static final Int2ObjectMap<CompoundTag> tileEntities = new Int2ObjectOpenHashMap<>();

    static {
        register("Furnace", 61, 62);
        register("Chest", 54, 146);
        register("EnderChest", 130);
        register("RecordPlayer", 84);
        register("Trap", 23); // Dispenser
        register("Dropper", 158);
        register("Sign", 63, 68);
        register("MobSpawner", 52);
        register("Music", 25); // Note Block
        register("Piston", 33, 34, 29, 36);
        register("Cauldron", 117); // Brewing stand
        register("EnchantTable", 116);
        register("Airportal", 119, 120); // End portal
        register("Beacon", 138);
        register("Skull", 144);
        register("DLDetector", 178, 151);
        register("Hopper", 154);
        register("Comparator", 149, 150);
        register("FlowerPot", 140);
        register("Banner", 176, 177);
        register("EndGateway", 209);
        register("Control", 137);
    }

    private static void register(String name, int... ids) {
        for (int id : ids) {
            CompoundTag comp = new CompoundTag();
            comp.put("id", new StringTag(name));
            tileEntities.put(id, comp);
        }
    }

    public static boolean isTileEntity(int block) {
        return tileEntities.containsKey(block);
    }

    public static CompoundTag createTileEntity(int x, int y, int z, int block) {
        CompoundTag originalTag = tileEntities.get(block);
        if (originalTag != null) {
            CompoundTag tag = originalTag.copy();
            tag.put("x", new IntTag(x));
            tag.put("y", new IntTag(y));
            tag.put("z", new IntTag(z));
            return tag;
        }
        return null;
    }
}
