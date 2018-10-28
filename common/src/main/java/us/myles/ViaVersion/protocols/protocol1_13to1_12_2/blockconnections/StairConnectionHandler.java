package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.*;

public class StairConnectionHandler extends ConnectionHandler{

    private static HashSet<String> baseStairs = new HashSet<>();

    private static Map<Integer, WrappedBlockData> blockData = new HashMap<>();

    static void init() {
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

        StairConnectionHandler connectionHandler = new StairConnectionHandler();
        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            if (baseStairs.contains(key)) {
                blockData.put(blockState.getValue(), WrappedBlockData.fromString(blockState.getKey()));
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
            }
        }
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
        blockdata.set("shape", getShape(user, position, blockdata));
        return blockdata.getBlockStateId();
    }

    private String getShape(UserConnection user, Position position, WrappedBlockData blockdata){
        BlockFace blockFace = BlockFace.valueOf(blockdata.getValue("facing").toUpperCase());
        WrappedBlockData blockData2 = WrappedBlockData.fromStateId(getBlockData(user, position.getRelative(blockFace)));
        if(isStair(blockData2.getMinecraftKey()) && blockdata.getValue("half").equals(blockData2.getValue("half"))){
            BlockFace blockFace2 = BlockFace.valueOf(blockData2.getValue("facing").toUpperCase());
            if(!isSameAxis(blockFace, blockFace2) && checkOpposite(user, blockdata, position, blockFace2.opposite())){
                if(blockFace2 == fixBlockFace(blockFace)){
                    return "outer_left";
                }
                return "outer_right";
            }
        }

        WrappedBlockData blockData3 = WrappedBlockData.fromStateId(getBlockData(user, position.getRelative(blockFace.opposite())));
        if(isStair(blockData3.getMinecraftKey()) && blockdata.getValue("half").equals(blockData3.getValue("half"))){
            BlockFace blockFace3 = BlockFace.valueOf(blockData3.getValue("facing").toUpperCase());
            if(!isSameAxis(blockFace, blockFace3) && checkOpposite(user, blockdata, position, blockFace3)){
                if(blockFace3 == fixBlockFace(blockFace)){
                    return "inner_left";
                }
                return "inner_right";
            }
        }
        return "straight";
    }

    private boolean isSameAxis(BlockFace face1, BlockFace face2){
        return Math.abs(face1.getModX()) == Math.abs(face2.getModX()) && Math.abs(face1.getModY()) == Math.abs(face2.getModY()) && Math.abs(face1.getModZ()) == Math.abs(face2.getModZ());
    }

    private boolean checkOpposite(UserConnection user, WrappedBlockData blockdata1, Position position, BlockFace face){
        WrappedBlockData data = WrappedBlockData.fromStateId(getBlockData(user, position.getRelative(face)));
        return !isStair(data.getMinecraftKey()) || !data.getValue("facing").equals(blockdata1.getValue("facing")) || !data.getValue("half").equals(blockdata1.getValue("half"));
    }

    private boolean isStair(String key){
        return baseStairs.contains(key);
    }

    private BlockFace fixBlockFace(BlockFace face) {
        switch (face.ordinal()) {
            case 0:
                return BlockFace.WEST;
            case 1:
                return BlockFace.EAST;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            default:
                return BlockFace.NORTH;
        }
    }

}
