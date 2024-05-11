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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StairConnectionHandler implements ConnectionHandler {
    private static final Int2ObjectMap<StairData> STAIR_DATA_MAP = new Int2ObjectOpenHashMap<>();
    private static final Map<Short, Integer> CONNECTED_BLOCKS = new HashMap<>();

    static ConnectionData.ConnectorInitAction init() {
        final List<String> baseStairs = new LinkedList<>();
        baseStairs.add("minecraft:oak_stairs");
        baseStairs.add("minecraft:cobblestone_stairs");
        baseStairs.add("minecraft:brick_stairs");
        baseStairs.add("minecraft:stone_brick_stairs");
        baseStairs.add("minecraft:nether_brick_stairs");
        baseStairs.add("minecraft:sandstone_stairs");
        baseStairs.add("minecraft:spruce_stairs");
        baseStairs.add("minecraft:birch_stairs");
        baseStairs.add("minecraft:jungle_stairs");
        baseStairs.add("minecraft:quartz_stairs");
        baseStairs.add("minecraft:acacia_stairs");
        baseStairs.add("minecraft:dark_oak_stairs");
        baseStairs.add("minecraft:red_sandstone_stairs");
        baseStairs.add("minecraft:purpur_stairs");
        baseStairs.add("minecraft:prismarine_stairs");
        baseStairs.add("minecraft:prismarine_brick_stairs");
        baseStairs.add("minecraft:dark_prismarine_stairs");

        final StairConnectionHandler connectionHandler = new StairConnectionHandler();
        return blockData -> {
            int type = baseStairs.indexOf(blockData.getMinecraftKey());
            if (type == -1) return;

            if (blockData.getValue("waterlogged").equals("true")) return;

            byte shape;
            switch (blockData.getValue("shape")) {
                case "straight":
                    shape = 0;
                    break;
                case "inner_left":
                    shape = 1;
                    break;
                case "inner_right":
                    shape = 2;
                    break;
                case "outer_left":
                    shape = 3;
                    break;
                case "outer_right":
                    shape = 4;
                    break;
                default:
                    return;
            }

            StairData stairData = new StairData(
                    blockData.getValue("half").equals("bottom"),
                    shape, (byte) type,
                    BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT))
            );

            STAIR_DATA_MAP.put(blockData.getSavedBlockStateId(), stairData);
            CONNECTED_BLOCKS.put(getStates(stairData), blockData.getSavedBlockStateId());

            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
        };
    }

    private static short getStates(StairData stairData) {
        short s = 0;
        if (stairData.bottom()) s |= 1;
        s |= stairData.shape() << 1;
        s |= stairData.type() << 4;
        s |= stairData.facing().ordinal() << 9;
        return s;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        StairData stairData = STAIR_DATA_MAP.get(blockState);
        if (stairData == null) return blockState;

        short s = 0;
        if (stairData.bottom()) s |= 1;
        s |= getShape(user, position, stairData) << 1;
        s |= stairData.type() << 4;
        s |= stairData.facing().ordinal() << 9;

        Integer newBlockState = CONNECTED_BLOCKS.get(s);
        return newBlockState == null ? blockState : newBlockState;
    }

    private int getShape(UserConnection user, Position position, StairData stair) {
        BlockFace facing = stair.facing();

        StairData relativeStair = STAIR_DATA_MAP.get(getBlockData(user, position.getRelative(facing)));
        if (relativeStair != null && relativeStair.bottom() == stair.bottom()) {
            BlockFace facing2 = relativeStair.facing();
            if (facing.axis() != facing2.axis() && checkOpposite(user, stair, position, facing2.opposite())) {
                return facing2 == rotateAntiClockwise(facing) ? 3 : 4; // outer_left : outer_right
            }
        }

        relativeStair = STAIR_DATA_MAP.get(getBlockData(user, position.getRelative(facing.opposite())));
        if (relativeStair != null && relativeStair.bottom() == stair.bottom()) {
            BlockFace facing2 = relativeStair.facing();
            if (facing.axis() != facing2.axis() && checkOpposite(user, stair, position, facing2)) {
                return facing2 == rotateAntiClockwise(facing) ? 1 : 2; // inner_left : inner_right
            }
        }

        return 0; // straight
    }

    private boolean checkOpposite(UserConnection user, StairData stair, Position position, BlockFace face) {
        StairData relativeStair = STAIR_DATA_MAP.get(getBlockData(user, position.getRelative(face)));
        return relativeStair == null || relativeStair.facing() != stair.facing() || relativeStair.bottom() != stair.bottom();
    }

    private BlockFace rotateAntiClockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            case WEST -> BlockFace.SOUTH;
            default -> face;
        };
    }

    private record StairData(boolean bottom, byte shape, byte type, BlockFace facing) {
    }
}
