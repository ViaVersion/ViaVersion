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

public class ChorusPlantConnectionHandler extends AbstractFenceConnectionHandler {
    private final int endstone;

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
        ChorusPlantConnectionHandler handler = new ChorusPlantConnectionHandler();
        actions.add(handler.getInitAction("minecraft:chorus_plant"));
        actions.add(handler.getExtraAction());
        return actions;
    }

    public ChorusPlantConnectionHandler() {
        super(null);
        endstone = ConnectionData.getId("minecraft:end_stone");
    }

    private ConnectionData.ConnectorInitAction getExtraAction() {
        return blockData -> {
            if (blockData.getMinecraftKey().equals("minecraft:chorus_flower")) {
                getBlockStates().add(blockData.getSavedBlockStateId());
            }
        };
    }

    @Override
    protected byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 16;
        if (blockData.getValue("down").equals("true")) states |= 32;
        return states;
    }

    @Override
    protected byte statesSize() {
        return 64;
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (connects(BlockFace.TOP, getBlockData(user, position.getRelative(BlockFace.TOP)), false)) states |= 16;
        if (connects(BlockFace.BOTTOM, getBlockData(user, position.getRelative(BlockFace.BOTTOM)), false)) states |= 32;
        return states;
    }

    @Override
    protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
        return getBlockStates().contains(blockState) || (side == BlockFace.BOTTOM && blockState == endstone);
    }
}
