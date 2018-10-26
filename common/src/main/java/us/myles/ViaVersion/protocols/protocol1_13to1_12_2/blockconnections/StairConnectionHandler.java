package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.*;

public class StairConnectionHandler extends ConnectionHandler{

    private static final List<BlockFace> blockFaces = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    private static HashSet<String> baseStairs = new HashSet<>();

    private static Map<Integer, WrappedBlockData> blockData = new HashMap<>();
    private static Map<Pair<BlockFace, BlockFace>, String> shapeMappings = new HashMap<>();

    static void init() {
        baseStairs.add("minecraft:oak_stairs");

        shapeMappings.put(new Pair(BlockFace.NORTH, BlockFace.EAST), "right");
        shapeMappings.put(new Pair(BlockFace.EAST, BlockFace.SOUTH), "right");
        shapeMappings.put(new Pair(BlockFace.SOUTH, BlockFace.WEST), "right");
        shapeMappings.put(new Pair(BlockFace.WEST, BlockFace.NORTH), "right");

        shapeMappings.put(new Pair(BlockFace.NORTH, BlockFace.WEST), "left");
        shapeMappings.put(new Pair(BlockFace.EAST, BlockFace.NORTH), "left");
        shapeMappings.put(new Pair(BlockFace.SOUTH, BlockFace.EAST), "left");
        shapeMappings.put(new Pair(BlockFace.WEST, BlockFace.SOUTH), "left");

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

    public boolean isStair(UserConnection user, Position position, BlockFace blockFace){
        int blockState = getBlockData(user, position.getRelative(blockFace));
        if(blockData.containsKey(blockState)){
            return true;
        }
        return false;
    }

    public BlockFace getFacing(UserConnection user, Position position, String face){
        for (BlockFace blockFace : blockFaces) {
            int blockState = getBlockData(user, position.getRelative(blockFace));
            if(blockData.containsKey(blockState)){
                WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
                if(blockdata.getValue("facing").equals(face)){
                    return blockFace;
                }
            }
        }

        return null;
    }

    private String getShape(UserConnection user, Position position, WrappedBlockData blockdata){
        BlockFace blockFace = getFacing(user, position, blockdata.getValue("facing"));
        if(blockFace != null){
            int index = blockFaces.indexOf(blockFace);
            BlockFace facing = BlockFace.valueOf(blockdata.getValue("facing").toUpperCase());
            String off = connect(user, position, blockFace, getNextBlockFace(index - 1), facing);
            if(off != null){
                return off;
            }
            String on = connect(user, position, blockFace, getNextBlockFace(index + 1), facing);
            if(on != null){
                return on;
            }
        }
        return "straight";
    }

    private String connect(UserConnection user, Position position, BlockFace main, BlockFace off, BlockFace facing){
        int blockState = getBlockData(user, position.getRelative(off));
        if(blockData.containsKey(blockState)){
            WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
            BlockFace blockFace = BlockFace.valueOf(blockdata.getValue("facing").toUpperCase());
            if( blockdata.getValue("shape").equals("straight")){
                return getType(position.getRelative(main).getRelative(facing), position.getRelative(off).getRelative(blockFace)) + "_" +  shapeMappings.get(new Pair(facing, blockFace));
            }
            return null;
        }
        return null;
    }

    public BlockFace getNextBlockFace(int i){
        if(i < 0){
            i = blockFaces.size() + i;
        }
        if(i >= blockFaces.size()){
            i = i - blockFaces.size();
        }
        return blockFaces.get(i);
    }

    public String getType(Position position, Position position2){
        return position.equals(position2) ? "outer" : "inner";
    }




}
