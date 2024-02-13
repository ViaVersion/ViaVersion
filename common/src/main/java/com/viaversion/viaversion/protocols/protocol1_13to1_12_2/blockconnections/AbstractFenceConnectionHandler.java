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
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;

public abstract class AbstractFenceConnectionHandler extends ConnectionHandler {
    private static final StairConnectionHandler STAIR_CONNECTION_HANDLER = new StairConnectionHandler();
    private final IntSet blockStates = new IntOpenHashSet();
    private final int[] connectedBlockStates = new int[statesSize()];
    private final int blockConnectionsTypeId;

    protected AbstractFenceConnectionHandler(String blockConnections) {
        this.blockConnectionsTypeId = blockConnections != null ? BlockData.connectionTypeId(blockConnections) : -1;
        Arrays.fill(connectedBlockStates, -1);
    }

    ConnectionData.ConnectorInitAction getInitAction(final String key) {
        final AbstractFenceConnectionHandler handler = this;
        return blockData -> {
            if (key.equals(blockData.getMinecraftKey())) {
                if (blockData.hasData("waterlogged") && blockData.getValue("waterlogged").equals("true")) {
                    return;
                }

                blockStates.add(blockData.getSavedBlockStateId());
                ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                byte internalStateId = getStates(blockData);
                connectedBlockStates[internalStateId] = blockData.getSavedBlockStateId();
            }
        };
    }

    protected byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        if (blockData.getValue("east").equals("true")) states |= 1;
        if (blockData.getValue("north").equals("true")) states |= 2;
        if (blockData.getValue("south").equals("true")) states |= 4;
        if (blockData.getValue("west").equals("true")) states |= 8;
        return states;
    }

    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = 0;
        boolean pre1_12 = user.getProtocolInfo().serverProtocolVersion().olderThan(ProtocolVersion.v1_12);
        if (connects(BlockFace.EAST, getBlockData(user, position.getRelative(BlockFace.EAST)), pre1_12)) states |= 1;
        if (connects(BlockFace.NORTH, getBlockData(user, position.getRelative(BlockFace.NORTH)), pre1_12)) states |= 2;
        if (connects(BlockFace.SOUTH, getBlockData(user, position.getRelative(BlockFace.SOUTH)), pre1_12)) states |= 4;
        if (connects(BlockFace.WEST, getBlockData(user, position.getRelative(BlockFace.WEST)), pre1_12)) states |= 8;
        return states;
    }

    protected byte statesSize() {
        return 16;
    }

    @Override
    public int getBlockData(UserConnection user, Position position) {
        return STAIR_CONNECTION_HANDLER.connect(user, position, super.getBlockData(user, position));
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        final int newBlockState = connectedBlockStates[getStates(user, position, blockState)];
        return newBlockState == -1 ? blockState : newBlockState;
    }

    protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
        if (blockStates.contains(blockState)) return true;
        if (blockConnectionsTypeId == -1) return false;

        BlockData blockData = ConnectionData.blockConnectionData.get(blockState);
        return blockData != null && blockData.connectsTo(blockConnectionsTypeId, side.opposite(), pre1_12);
    }

    public IntSet getBlockStates() {
        return blockStates;
    }
}
