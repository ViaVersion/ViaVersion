package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Marco Neuhaus on 23.09.2018 for the Project ViaVersionGerry.
 */
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
                doors.put(blockState.getValue(), blockState.getKey());
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
            }
        }
    }

    @Override
    public int connect(Position position, int blockState, ConnectionData connectionData) {
        int blockBelow = connectionData.get(position.getRelative(BlockFace.BOTTOM));
        int blockAbove = connectionData.get(position.getRelative(BlockFace.TOP));
        String data;
        if(doors.containsKey(blockBelow)){
            String hinge = doors.get(blockState);
            hinge = hinge.split("hinge=")[1];
            hinge = hinge.substring(0, hinge.indexOf(','));
            data = doors.get(blockBelow).replace("half=lower", "half=upper").replace("hinge=right", "hinge="+hinge);
        }else if(doors.containsKey(blockAbove)){
            String hinge = doors.get(blockAbove);
            hinge = hinge.split("hinge=")[1];
            hinge = hinge.substring(0, hinge.indexOf(','));
            data = doors.get(blockState).replace("hinge=right", "hinge="+hinge);
        }else{
            data = doors.get(blockState);
        }
        return ConnectionData.getId(data);
    }
}
