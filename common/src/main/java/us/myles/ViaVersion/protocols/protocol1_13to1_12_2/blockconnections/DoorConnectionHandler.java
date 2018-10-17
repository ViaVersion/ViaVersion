package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoorConnectionHandler implements ConnectionHandler{

    private static Set<String> baseDoors = new HashSet<>();
    private static Map<Integer, String> doors = new HashMap<>();

    static void init() {
        baseDoors.add("minecraft:oak_door");
        baseDoors.add("minecraft:birch_door");
        baseDoors.add("minecraft:jungle_door");
        baseDoors.add("minecraft:dark_oak_door");
        baseDoors.add("minecraft:acacia_door");
        baseDoors.add("minecraft:spruce_door");
        baseDoors.add("minecraft:iron_door");

        DoorConnectionHandler connectionHandler = new DoorConnectionHandler();
        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            if (baseDoors.contains(key)) {
                doors.put(blockState.getValue(), key);
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
            }
        }
    }

    @Override
    public int connect(Position position, int blockState, ConnectionData connectionData) {
        int blockId = connectionData.get(position.getRelative(BlockFace.BOTTOM));
        WrappedBlockdata blockdata = WrappedBlockdata.fromStateId(blockState);
        if(doors.containsKey(blockState) && blockdata.getValue("half").equals("lower")){
            blockdata.set("hinge", "left");
            if(isDoor(position, BlockFace.SOUTH, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("west") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.EAST, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("south") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.WEST, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("north") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.NORTH, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("east") ? "right" : "left");
            }
            return blockdata.getBlockStateId();
        }
        if(doors.containsKey(blockId)) {
            blockdata = WrappedBlockdata.fromStateId(blockId);
            blockdata.set("half", "upper");
            blockdata.set("hinge", "left");
            if(isDoor(position, BlockFace.SOUTH, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("west") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.EAST, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("south") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.WEST, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("north") ? "right" : "left");
            }
            if(isDoor(position, BlockFace.NORTH, connectionData)){
                blockdata.set("hinge", blockdata.getValue("facing").equals("east") ? "right" : "left");
            }
            return blockdata.getBlockStateId();
        }else{
            return blockState;
        }
    }

    private boolean isDoor(Position position, BlockFace blockFace, ConnectionData connectionData){
        int blockState = connectionData.get(position.getRelative(blockFace));
        if(doors.containsKey(blockState)){
            int current = connectionData.get(position);
            return doors.get(blockState).equals(doors.get(current));
        }
        return false;
    }
}
