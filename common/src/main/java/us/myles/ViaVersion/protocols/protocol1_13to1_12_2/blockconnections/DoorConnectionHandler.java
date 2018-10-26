package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoorConnectionHandler extends ConnectionHandler{

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
    public int connect(UserConnection user, Position position, int blockState) {
        int blockBelowId = getBlockData(user, position.getRelative(BlockFace.BOTTOM));
        int blockAboveId = getBlockData(user, position.getRelative(BlockFace.TOP));
        WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
        if(doors.containsKey(blockState)){
            if (blockdata.getValue("half").equals("lower")){
                if(doors.containsKey(blockAboveId)){
                    WrappedBlockData blockAboveData = WrappedBlockData.fromStateId(blockAboveId);
                    blockdata.set("hinge", blockAboveData.getValue("hinge"));
                    blockdata.set("powered", blockAboveData.getValue("powered"));
                }
            }else{
                if(doors.containsKey(blockBelowId)){
                    WrappedBlockData blockBelowData = WrappedBlockData.fromStateId(blockBelowId);
                    blockdata.set("open", blockBelowData.getValue("open"));
                    blockdata.set("facing", blockBelowData.getValue("facing"));
                }
            }
        }
        return blockdata.getBlockStateId();
    }
}
