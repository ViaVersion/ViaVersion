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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.api.minecraft.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class BlockData {
    private final Map<String, boolean[]> connectData = new HashMap<>();

    public void put(String key, boolean[] booleans) {
        connectData.put(key, booleans);
    }

    public boolean connectsTo(String blockConnection, BlockFace face, boolean pre1_12AbstractFence) {
        boolean[] booleans = null;
        if (pre1_12AbstractFence) {
            booleans = connectData.get("allFalseIfStairPre1_12"); // https://minecraft.gamepedia.com/Java_Edition_1.12
        }
        if (booleans == null) {
            booleans = connectData.get(blockConnection);
        }
        return booleans != null && booleans[face.ordinal()];
    }
}
