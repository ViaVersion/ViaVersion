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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.BlockEntityProvider;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;

public class FlowerPotHandler implements BlockEntityProvider.BlockEntityHandler {
    private static final int EMPTY_POT = 5265;
    private static final Map<String, Byte> STRING_TO_BYTE_ID = new HashMap<>();
    private static final Map<IntIdPair, Integer> FLOWERS = new HashMap<>();

    static {
        register("air", (byte) 0, (byte) 0, 5265);
        register("sapling", (byte) 6, (byte) 0, 5266);
        register("sapling", (byte) 6, (byte) 1, 5267);
        register("sapling", (byte) 6, (byte) 2, 5268);
        register("sapling", (byte) 6, (byte) 3, 5269);
        register("sapling", (byte) 6, (byte) 4, 5270);
        register("sapling", (byte) 6, (byte) 5, 5271);
        register("tallgrass", (byte) 31, (byte) 2, 5272);
        register("yellow_flower", (byte) 37, (byte) 0, 5273);
        register("red_flower", (byte) 38, (byte) 0, 5274);
        register("red_flower", (byte) 38, (byte) 1, 5275);
        register("red_flower", (byte) 38, (byte) 2, 5276);
        register("red_flower", (byte) 38, (byte) 3, 5277);
        register("red_flower", (byte) 38, (byte) 4, 5278);
        register("red_flower", (byte) 38, (byte) 5, 5279);
        register("red_flower", (byte) 38, (byte) 6, 5280);
        register("red_flower", (byte) 38, (byte) 7, 5281);
        register("red_flower", (byte) 38, (byte) 8, 5282);
        register("red_mushroom", (byte) 40, (byte) 0, 5283);
        register("brown_mushroom", (byte) 39, (byte) 0, 5284);
        register("deadbush", (byte) 32, (byte) 0, 5285);
        register("cactus", (byte) 81, (byte) 0, 5286);

    }

    private static void register(String identifier, byte blockId, byte blockData, int newId) {
        STRING_TO_BYTE_ID.put(identifier, blockId);
        FLOWERS.put(new IntIdPair(blockId, blockData), newId);
    }

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        // Convert item to String without namespace or to Byte
        Tag itemTag = tag.get("Item");
        byte item = 0;
        if (itemTag instanceof StringTag stringTag) {
            item = STRING_TO_BYTE_ID.getOrDefault(Key.stripMinecraftNamespace(stringTag.getValue()), (byte) 0);
        } else if (itemTag instanceof NumberTag numberTag) {
            item = numberTag.asByte();
        }

        byte data = 0;
        if (tag.get("Data") instanceof NumberTag dataTag) {
            data = dataTag.asByte();
        }

        Integer flower = FLOWERS.get(new IntIdPair(item, data));
        if (flower != null) {
            return flower;
        }

        flower = FLOWERS.get(new IntIdPair(item, (byte) 0));
        if (flower != null) {
            return flower;
        }

        return EMPTY_POT; // Fallback to empty pot
    }

    private record IntIdPair(int id, byte data) {
    }
}
