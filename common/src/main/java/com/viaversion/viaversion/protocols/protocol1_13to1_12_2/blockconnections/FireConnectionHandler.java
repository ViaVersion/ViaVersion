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
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashSet;
import java.util.Set;

public class FireConnectionHandler extends ConnectionHandler {
    private static final String[] WOOD_TYPES = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final int[] CONNECTED_BLOCKS = new int[32];
    private static final IntSet FLAMMABLE_BLOCKS = new IntOpenHashSet();

    private static void addWoodTypes(Set<String> set, String suffix) {
        for (String woodType : WOOD_TYPES) {
            set.add(Key.namespaced(woodType + suffix));
        }
    }

    static ConnectionData.ConnectorInitAction init() {
        Set<String> flammabeIds = new HashSet<>();
        flammabeIds.add("minecraft:tnt");
        flammabeIds.add("minecraft:vine");
        flammabeIds.add("minecraft:bookshelf");
        flammabeIds.add("minecraft:hay_block");
        flammabeIds.add("minecraft:deadbush");
        addWoodTypes(flammabeIds, "_slab");
        addWoodTypes(flammabeIds, "_log");
        addWoodTypes(flammabeIds, "_planks");
        addWoodTypes(flammabeIds, "_leaves");
        addWoodTypes(flammabeIds, "_fence");
        addWoodTypes(flammabeIds, "_fence_gate");
        addWoodTypes(flammabeIds, "_stairs");

        FireConnectionHandler connectionHandler = new FireConnectionHandler();
        return blockData -> {
            String key = blockData.getMinecraftKey();
            if (key.contains("_wool") || key.contains("_carpet") || flammabeIds.contains(key)) {
                FLAMMABLE_BLOCKS.add(blockData.getSavedBlockStateId());
            } else if (key.equals("minecraft:fire")) {
                int id = blockData.getSavedBlockStateId();
                CONNECTED_BLOCKS[getStates(blockData)] = id;
                ConnectionData.connectionHandlerMap.put(id, connectionHandler);
            }
        };
    }

    private static byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        if (blockData.getValue("east").equals("true")) states |= 1;
        if (blockData.getValue("north").equals("true")) states |= 2;
        if (blockData.getValue("south").equals("true")) states |= 4;
        if (blockData.getValue("up").equals("true")) states |= 8;
        if (blockData.getValue("west").equals("true")) states |= 16;
        return states;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        byte states = 0;
        if (FLAMMABLE_BLOCKS.contains(getBlockData(user, position.getRelative(BlockFace.EAST)))) states |= 1;
        if (FLAMMABLE_BLOCKS.contains(getBlockData(user, position.getRelative(BlockFace.NORTH)))) states |= 2;
        if (FLAMMABLE_BLOCKS.contains(getBlockData(user, position.getRelative(BlockFace.SOUTH)))) states |= 4;
        if (FLAMMABLE_BLOCKS.contains(getBlockData(user, position.getRelative(BlockFace.TOP)))) states |= 8;
        if (FLAMMABLE_BLOCKS.contains(getBlockData(user, position.getRelative(BlockFace.WEST)))) states |= 16;
        return CONNECTED_BLOCKS[states];
    }
}
