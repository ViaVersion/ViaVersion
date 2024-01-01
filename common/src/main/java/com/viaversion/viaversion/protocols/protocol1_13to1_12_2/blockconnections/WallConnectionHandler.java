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
import java.util.ArrayList;
import java.util.List;

public class WallConnectionHandler extends AbstractFenceConnectionHandler {
    private static final BlockFace[] BLOCK_FACES = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};
    private static final int[] OPPOSITES = {3, 2, 1, 0};

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
        actions.add(new WallConnectionHandler("cobbleWall").getInitAction("minecraft:cobblestone_wall"));
        actions.add(new WallConnectionHandler("cobbleWall").getInitAction("minecraft:mossy_cobblestone_wall"));
        return actions;
    }


    public WallConnectionHandler(String blockConnections) {
        super(blockConnections);
    }

    @Override
    protected byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 16;
        return states;
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (up(user, position)) states |= 16;
        return states;
    }

    @Override
    protected byte statesSize() {
        return 32;
    }

    public boolean up(UserConnection user, Position position) {
        if (isWall(getBlockData(user, position.getRelative(BlockFace.BOTTOM))) || isWall(getBlockData(user, position.getRelative(BlockFace.TOP))))
            return true;
        int blockFaces = getBlockFaces(user, position);
        if (blockFaces == 0 || blockFaces == 0xF) return true;
        for (int i = 0; i < BLOCK_FACES.length; i++) {
            if ((blockFaces & (1 << i)) != 0 && (blockFaces & (1 << OPPOSITES[i])) == 0) return true;
        }
        return false;
    }

    private int getBlockFaces(UserConnection user, Position position) {
        int blockFaces = 0;
        for (int i = 0; i < BLOCK_FACES.length; i++) {
            if (isWall(getBlockData(user, position.getRelative(BLOCK_FACES[i])))) {
                blockFaces |= 1 << i;
            }
        }
        return blockFaces;
    }

    private boolean isWall(int id) {
        return getBlockStates().contains(id);
    }
}
