/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoorConnectionHandler implements ConnectionHandler {
    private static final Int2ObjectMap<DoorData> DOOR_DATA_MAP = new Int2ObjectOpenHashMap<>();
    private static final Map<Short, Integer> CONNECTED_STATES = new HashMap<>();

    static ConnectionData.ConnectorInitAction init() {
        final List<String> baseDoors = new LinkedList<>();
        baseDoors.add("minecraft:oak_door");
        baseDoors.add("minecraft:birch_door");
        baseDoors.add("minecraft:jungle_door");
        baseDoors.add("minecraft:dark_oak_door");
        baseDoors.add("minecraft:acacia_door");
        baseDoors.add("minecraft:spruce_door");
        baseDoors.add("minecraft:iron_door");

        final DoorConnectionHandler connectionHandler = new DoorConnectionHandler();
        return blockData -> {
            int type = baseDoors.indexOf(blockData.getMinecraftKey());
            if (type == -1) return;

            int id = blockData.getSavedBlockStateId();

            DoorData doorData = new DoorData(
                blockData.getValue("half").equals("lower"),
                blockData.getValue("hinge").equals("right"),
                blockData.getValue("powered").equals("true"),
                blockData.getValue("open").equals("true"),
                BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)),
                type
            );

            DOOR_DATA_MAP.put(id, doorData);

            CONNECTED_STATES.put(getStates(doorData), id);

            ConnectionData.connectionHandlerMap.put(id, connectionHandler);
        };
    }

    private static short getStates(DoorData doorData) {
        short s = 0;
        if (doorData.lower()) s |= 1;
        if (doorData.open()) s |= 2;
        if (doorData.powered()) s |= 4;
        if (doorData.rightHinge()) s |= 8;
        s |= doorData.facing().ordinal() << 4;
        s |= (doorData.type() & 0x7) << 6;
        return s;
    }

    @Override
    public int connect(UserConnection user, BlockPosition position, int blockState) {
        DoorData doorData = DOOR_DATA_MAP.get(blockState);
        if (doorData == null) return blockState;
        short s = 0;
        s |= (doorData.type() & 0x7) << 6;
        if (doorData.lower()) {
            DoorData upperHalf = DOOR_DATA_MAP.get(getBlockData(user, position.getRelative(BlockFace.TOP)));
            if (upperHalf == null) return blockState;
            s |= 1;
            if (doorData.open()) s |= 2;
            if (upperHalf.powered()) s |= 4;
            if (upperHalf.rightHinge()) s |= 8;
            s |= doorData.facing().ordinal() << 4;
        } else {
            DoorData lowerHalf = DOOR_DATA_MAP.get(getBlockData(user, position.getRelative(BlockFace.BOTTOM)));
            if (lowerHalf == null) return blockState;
            if (lowerHalf.open()) s |= 2;
            if (doorData.powered()) s |= 4;
            if (doorData.rightHinge()) s |= 8;
            s |= lowerHalf.facing().ordinal() << 4;
        }

        Integer newBlockState = CONNECTED_STATES.get(s);
        return newBlockState == null ? blockState : newBlockState;
    }

    private record DoorData(boolean lower, boolean rightHinge, boolean powered,
                            boolean open, BlockFace facing, int type) {
    }
}
