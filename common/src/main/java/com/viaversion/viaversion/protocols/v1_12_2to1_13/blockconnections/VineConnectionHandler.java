/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class VineConnectionHandler implements ConnectionHandler {
    private static final IntSet VINES = new IntOpenHashSet();

    static ConnectionData.ConnectorInitAction init() {
        final VineConnectionHandler connectionHandler = new VineConnectionHandler();
        return blockData -> {
            if (!blockData.getMinecraftKey().equals("minecraft:vine")) return;

            VINES.add(blockData.getSavedBlockStateId());
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
        };
    }

    @Override
    public int connect(UserConnection user, BlockPosition position, int blockState) {
        if (isAttachedToBlock(user, position)) return blockState;

        BlockPosition upperPos = position.getRelative(BlockFace.TOP);
        int upperBlock = getBlockData(user, upperPos);
        if (VINES.contains(upperBlock) && isAttachedToBlock(user, upperPos)) return blockState;

        // Map to air if not attached to block, and upper block is also not a vine attached to a block
        return 0;
    }

    private boolean isAttachedToBlock(UserConnection user, BlockPosition position) {
        return isAttachedToBlock(user, position, BlockFace.EAST)
            || isAttachedToBlock(user, position, BlockFace.WEST)
            || isAttachedToBlock(user, position, BlockFace.NORTH)
            || isAttachedToBlock(user, position, BlockFace.SOUTH);
    }

    private boolean isAttachedToBlock(UserConnection user, BlockPosition position, BlockFace blockFace) {
        return ConnectionData.OCCLUDING_STATES.contains(getBlockData(user, position.getRelative(blockFace)));
    }
}
