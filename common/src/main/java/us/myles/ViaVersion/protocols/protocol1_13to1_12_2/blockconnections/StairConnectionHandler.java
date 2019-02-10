package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StairConnectionHandler extends ConnectionHandler {
    private static Map<Integer, StairData> stairDataMap = new HashMap<>();
    private static Map<Short, Integer> connectedBlocks = new HashMap<>();

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
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                int type = baseStairs.indexOf(blockData.getMinecraftKey());
                if (type == -1) return;

                if (blockData.getValue("waterlogged").equals("true")) return;

                byte shape;
                switch (blockData.getValue("shape")) {
                    case "straight": shape = 0; break;
                    case "inner_left": shape = 1; break;
                    case "inner_right": shape = 2; break;
                    case "outer_left": shape = 3; break;
                    case "outer_right": shape = 4; break;
                    default: return;
                }

                StairData stairData = new StairData(
                        blockData.getValue("half").equals("bottom"),
                        shape, (byte) type,
                        BlockFace.valueOf(blockData.getValue("facing").toUpperCase())
                );

                stairDataMap.put(blockData.getSavedBlockStateId(), stairData);
                connectedBlocks.put(getStates(stairData), blockData.getSavedBlockStateId());

                ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
            }
        };
    }

    private static short getStates(StairData stairData) {
        short s = 0;
        if (stairData.isBottom()) s |= 1;
        s |= stairData.getShape() << 1;
        s |= stairData.getType() << 4;
        s |= stairData.getFacing().ordinal() << 9;
        return s;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        StairData stairData = stairDataMap.get(blockState);
        if (stairData == null) return blockState;

        short s = 0;
        if (stairData.isBottom()) s |= 1;
        s |= getShape(user, position, stairData) << 1;
        s |= stairData.getType() << 4;
        s |= stairData.getFacing().ordinal() << 9;

        Integer newBlockState = connectedBlocks.get(s);
        return newBlockState == null ? blockState : newBlockState;
    }

    private int getShape(UserConnection user, Position position, StairData stair) {
        BlockFace facing = stair.getFacing();

        StairData relativeStair = stairDataMap.get(getBlockData(user, position.getRelative(facing)));
        if (relativeStair != null && relativeStair.isBottom() == stair.isBottom()) {
            BlockFace facing2 = relativeStair.getFacing();
            if (facing.getAxis() != facing2.getAxis() && checkOpposite(user, stair, position, facing2.opposite())) {
                return facing2 == rotateAntiClockwise(facing) ? 3 : 4; // outer_left : outer_right
            }
        }

        relativeStair = stairDataMap.get(getBlockData(user, position.getRelative(facing.opposite())));
        if (relativeStair != null && relativeStair.isBottom() == stair.isBottom()) {
            BlockFace facing2 = relativeStair.getFacing();
            if (facing.getAxis() != facing2.getAxis() && checkOpposite(user, stair, position, facing2)) {
                return facing2 == rotateAntiClockwise(facing) ? 1 : 2; // inner_left : inner_right
            }
        }

        return 0; // straight
    }

    private boolean checkOpposite(UserConnection user, StairData stair, Position position, BlockFace face) {
        StairData relativeStair = stairDataMap.get(getBlockData(user, position.getRelative(face)));
        return relativeStair == null || relativeStair.getFacing() != stair.getFacing() || relativeStair.isBottom() != stair.isBottom();
    }

    private BlockFace rotateAntiClockwise(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case SOUTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.NORTH;
            case WEST:
                return BlockFace.SOUTH;
            default:
                return face;
        }
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class StairData {
        private final boolean bottom;
        private final byte shape, type;
        private final BlockFace facing;
    }
}
