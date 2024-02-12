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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.Position;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class RedstoneConnectionHandler extends ConnectionHandler {
    private static final IntSet REDSTONE = new IntOpenHashSet();
    private static final Int2IntMap CONNECTED_BLOCK_STATES = new Int2IntOpenHashMap(1296);
    private static final Int2IntMap POWER_MAPPINGS = new Int2IntOpenHashMap(1296);
    private static final int BLOCK_CONNECTION_TYPE_ID = BlockData.connectionTypeId("redstone");

    static ConnectionData.ConnectorInitAction init() {
        final RedstoneConnectionHandler connectionHandler = new RedstoneConnectionHandler();
        final String redstoneKey = "minecraft:redstone_wire";
        return blockData -> {
            if (!redstoneKey.equals(blockData.getMinecraftKey())) {
                return;
            }

            REDSTONE.add(blockData.getSavedBlockStateId());
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
            CONNECTED_BLOCK_STATES.put(getStates(blockData), blockData.getSavedBlockStateId());
            POWER_MAPPINGS.put(blockData.getSavedBlockStateId(), Integer.parseInt(blockData.getValue("power")));
        };
    }

    private static short getStates(WrappedBlockData data) {
        short b = 0;
        b |= getState(data.getValue("east"));
        b |= getState(data.getValue("north")) << 2;
        b |= getState(data.getValue("south")) << 4;
        b |= getState(data.getValue("west")) << 6;
        b |= Integer.parseInt(data.getValue("power")) << 8;
        return b;
    }

    private static int getState(String value) {
        switch (value) {
            case "none":
            default:
                return 0;
            case "side":
                return 1;
            case "up":
                return 2;
        }
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        short b = 0;
        b |= connects(user, position, BlockFace.EAST);
        b |= connects(user, position, BlockFace.NORTH) << 2;
        b |= connects(user, position, BlockFace.SOUTH) << 4;
        b |= connects(user, position, BlockFace.WEST) << 6;
        b |= POWER_MAPPINGS.get(blockState) << 8;
        return CONNECTED_BLOCK_STATES.getOrDefault(b, blockState);
    }

    private int connects(UserConnection user, Position position, BlockFace side) {
        final Position relative = position.getRelative(side);
        int blockState = getBlockData(user, relative);
        if (connects(side, blockState)) {
            return 1; //side
        }
        int up = getBlockData(user, relative.getRelative(BlockFace.TOP));
        if (REDSTONE.contains(up) && !ConnectionData.OCCLUDING_STATES.contains(getBlockData(user, position.getRelative(BlockFace.TOP)))) {
            return 2; //"up"
        }
        int down = getBlockData(user, relative.getRelative(BlockFace.BOTTOM));
        if (REDSTONE.contains(down) && !ConnectionData.OCCLUDING_STATES.contains(getBlockData(user, relative))) {
            return 1; //side
        }
        return 0; //none
    }

    private boolean connects(BlockFace side, int blockState) {
        final BlockData blockData = ConnectionData.blockConnectionData.get(blockState);
        return blockData != null && blockData.connectsTo(BLOCK_CONNECTION_TYPE_ID, side.opposite(), false);
    }
}
