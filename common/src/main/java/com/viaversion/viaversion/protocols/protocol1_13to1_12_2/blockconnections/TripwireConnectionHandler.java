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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Locale;

public class TripwireConnectionHandler extends ConnectionHandler {
    private static final Int2ObjectMap<TripwireData> TRIPWIRE_DATA_MAP = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<BlockFace> TRIPWIRE_HOOKS = new Int2ObjectArrayMap<>();
    private static final int[] CONNECTED_BLOCKS = new int[128];

    static ConnectionData.ConnectorInitAction init() {
        Arrays.fill(CONNECTED_BLOCKS, -1);
        final TripwireConnectionHandler connectionHandler = new TripwireConnectionHandler();
        return blockData -> {
            if (blockData.getMinecraftKey().equals("minecraft:tripwire_hook")) {
                TRIPWIRE_HOOKS.put(blockData.getSavedBlockStateId(), BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)));
            } else if (blockData.getMinecraftKey().equals("minecraft:tripwire")) {
                TripwireData tripwireData = new TripwireData(
                        blockData.getValue("attached").equals("true"),
                        blockData.getValue("disarmed").equals("true"),
                        blockData.getValue("powered").equals("true")
                );

                TRIPWIRE_DATA_MAP.put(blockData.getSavedBlockStateId(), tripwireData);
                CONNECTED_BLOCKS[getStates(blockData)] = blockData.getSavedBlockStateId();

                ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
            }
        };
    }

    private static byte getStates(WrappedBlockData blockData) {
        byte b = 0;
        if (blockData.getValue("attached").equals("true")) b |= 1;
        if (blockData.getValue("disarmed").equals("true")) b |= 2;
        if (blockData.getValue("powered").equals("true")) b |= 4;
        if (blockData.getValue("east").equals("true")) b |= 8;
        if (blockData.getValue("north").equals("true")) b |= 16;
        if (blockData.getValue("south").equals("true")) b |= 32;
        if (blockData.getValue("west").equals("true")) b |= 64;
        return b;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        TripwireData tripwireData = TRIPWIRE_DATA_MAP.get(blockState);
        if (tripwireData == null) return blockState;
        byte b = 0;
        if (tripwireData.isAttached()) b |= 1;
        if (tripwireData.isDisarmed()) b |= 2;
        if (tripwireData.isPowered()) b |= 4;

        int east = getBlockData(user, position.getRelative(BlockFace.EAST));
        int north = getBlockData(user, position.getRelative(BlockFace.NORTH));
        int south = getBlockData(user, position.getRelative(BlockFace.SOUTH));
        int west = getBlockData(user, position.getRelative(BlockFace.WEST));

        if (TRIPWIRE_DATA_MAP.containsKey(east) || TRIPWIRE_HOOKS.get(east) == BlockFace.WEST) {
            b |= 8;
        }
        if (TRIPWIRE_DATA_MAP.containsKey(north) || TRIPWIRE_HOOKS.get(north) == BlockFace.SOUTH) {
            b |= 16;
        }
        if (TRIPWIRE_DATA_MAP.containsKey(south) || TRIPWIRE_HOOKS.get(south) == BlockFace.NORTH) {
            b |= 32;
        }
        if (TRIPWIRE_DATA_MAP.containsKey(west) || TRIPWIRE_HOOKS.get(west) == BlockFace.EAST) {
            b |= 64;
        }

        int newBlockState = CONNECTED_BLOCKS[b];
        return newBlockState == -1 ? blockState : newBlockState;
    }

    private static final class TripwireData {
        private final boolean attached, disarmed, powered;

        private TripwireData(final boolean attached, final boolean disarmed, final boolean powered) {
            this.attached = attached;
            this.disarmed = disarmed;
            this.powered = powered;
        }

        public boolean isAttached() {
            return attached;
        }

        public boolean isDisarmed() {
            return disarmed;
        }

        public boolean isPowered() {
            return powered;
        }
    }
}
