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
import java.util.ArrayList;
import java.util.List;

public class WallConnectionHandler extends AbstractFenceConnectionHandler {

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
    protected byte getStates(UserConnection user, BlockPosition position) {
        byte states = super.getStates(user, position);
        if (up(user, position, states)) states |= 16;
        return states;
    }

    @Override
    protected byte statesSize() {
        return 32;
    }

    private boolean up(UserConnection user, BlockPosition position, byte states) {
        // Only straight walls with air above have no post, see BlockWall#getActualState
        boolean straight = states == (2 | 4) || states == (1 | 8);
        return !straight || getBlockData(user, position.getRelative(BlockFace.TOP)) != 0;
    }
}
