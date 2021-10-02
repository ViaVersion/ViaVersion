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
package com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.List;

/**
 * Mojang changed the way how tile entities inside chunk packets work in 1.10.1
 * It requires now to have all tile entity data included in the chunk packet, otherwise it'll crash.
 */
public class FakeTileEntity {
    private static final Int2ObjectMap<CompoundTag> tileEntities = new Int2ObjectOpenHashMap<>();

    static {
        register(Arrays.asList(61, 62), "Furnace");
        register(Arrays.asList(54, 146), "Chest");
        register(130, "EnderChest");
        register(84, "RecordPlayer");
        register(23, "Trap"); // Dispenser
        register(158, "Dropper");
        register(Arrays.asList(63, 68), "Sign");
        register(52, "MobSpawner");
        register(25, "Music"); // Note Block
        register(Arrays.asList(33, 34, 29, 36), "Piston");
        register(117, "Cauldron"); // Brewing stand
        register(116, "EnchantTable");
        register(Arrays.asList(119, 120), "Airportal"); // End portal
        register(138, "Beacon");
        register(144, "Skull");
        register(Arrays.asList(178, 151), "DLDetector");
        register(154, "Hopper");
        register(Arrays.asList(149, 150), "Comparator");
        register(140, "FlowerPot");
        register(Arrays.asList(176, 177), "Banner");
        register(209, "EndGateway");
        register(137, "Control");
    }

    private static void register(int material, String name) {
        CompoundTag comp = new CompoundTag();
        comp.put(name, new StringTag());
        tileEntities.put(material, comp);
    }

    private static void register(List<Integer> materials, String name) {
        for (int id : materials) {
            register(id, name);
        }
    }

    public static boolean hasBlock(int block) {
        return tileEntities.containsKey(block);
    }

    public static CompoundTag getFromBlock(int x, int y, int z, int block) {
        CompoundTag originalTag = tileEntities.get(block);
        if (originalTag != null) {
            CompoundTag tag = originalTag.clone();
            tag.put("x", new IntTag(x));
            tag.put("y", new IntTag(y));
            tag.put("z", new IntTag(z));
            return tag;
        }
        return null;
    }
}
