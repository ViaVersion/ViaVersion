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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Locale;

class ChestConnectionHandler extends ConnectionHandler {
    private static final Int2ObjectMap<BlockFace> CHEST_FACINGS = new Int2ObjectOpenHashMap<>();
    private static final int[] CONNECTED_STATES = new int[32];
    private static final IntSet TRAPPED_CHESTS = new IntOpenHashSet();

    static ConnectionData.ConnectorInitAction init() {
        Arrays.fill(CONNECTED_STATES, -1);
        final ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
        return blockData -> {
            if (!blockData.getMinecraftKey().equals("minecraft:chest") && !blockData.getMinecraftKey().equals("minecraft:trapped_chest")) {
                return;
            }
            if (blockData.getValue("waterlogged").equals("true")) {
                return;
            }
            CHEST_FACINGS.put(blockData.getSavedBlockStateId(), BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)));
            if (blockData.getMinecraftKey().equalsIgnoreCase("minecraft:trapped_chest")) {
                TRAPPED_CHESTS.add(blockData.getSavedBlockStateId());
            }
            CONNECTED_STATES[getStates(blockData)] = blockData.getSavedBlockStateId();
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
        };
    }

    private static Byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        String type = blockData.getValue("type");
        if (type.equals("left")) states |= 1;
        if (type.equals("right")) states |= 2;
        states |= (BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)).ordinal() << 2);
        if (blockData.getMinecraftKey().equals("minecraft:trapped_chest")) states |= 16;
        return states;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        BlockFace facing = CHEST_FACINGS.get(blockState);
        byte states = 0;
        states |= (facing.ordinal() << 2);

        boolean trapped = TRAPPED_CHESTS.contains(blockState);
        if (trapped) {
            states |= 16;
        }

        int relative;
        if (CHEST_FACINGS.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.NORTH))) && trapped == TRAPPED_CHESTS.contains(relative)) {
            states |= facing == BlockFace.WEST ? 1 : 2;
        } else if (CHEST_FACINGS.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.SOUTH))) && trapped == TRAPPED_CHESTS.contains(relative)) {
            states |= facing == BlockFace.EAST ? 1 : 2;
        } else if (CHEST_FACINGS.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.WEST))) && trapped == TRAPPED_CHESTS.contains(relative)) {
            states |= facing == BlockFace.NORTH ? 2 : 1;
        } else if (CHEST_FACINGS.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.EAST))) && trapped == TRAPPED_CHESTS.contains(relative)) {
            states |= facing == BlockFace.SOUTH ? 2 : 1;
        }

        int newBlockState = CONNECTED_STATES[states];
        return newBlockState == -1 ? blockState : newBlockState;
    }
}
