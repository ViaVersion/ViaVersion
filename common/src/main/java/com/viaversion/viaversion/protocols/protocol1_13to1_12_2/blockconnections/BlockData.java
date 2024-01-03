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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import java.util.List;

public final class BlockData {
    private static final List<String> CONNECTION_TYPES = Arrays.asList("fence", "netherFence", "pane", "cobbleWall", "redstone", "allFalseIfStairPre1_12");
    private static final int MAGIC_STAIRS_ID = connectionTypeId("allFalseIfStairPre1_12");
    private final Int2ObjectMap<boolean[]> connectData = new Int2ObjectArrayMap<>();

    public void put(final int blockConnectionTypeId, final boolean[] booleans) {
        connectData.put(blockConnectionTypeId, booleans);
    }

    public boolean connectsTo(final int blockConnectionTypeId, final BlockFace face, final boolean pre1_12AbstractFence) {
        if (pre1_12AbstractFence && connectData.containsKey(MAGIC_STAIRS_ID)) {
            return false;
        }

        final boolean[] booleans = connectData.get(blockConnectionTypeId);
        return booleans != null && booleans[face.ordinal()];
    }

    public static int connectionTypeId(final String blockConnection) {
        final int connectionTypeId = CONNECTION_TYPES.indexOf(blockConnection);
        Preconditions.checkArgument(connectionTypeId != -1, "Unknown connection type: " + blockConnection);
        return connectionTypeId;
    }
}
