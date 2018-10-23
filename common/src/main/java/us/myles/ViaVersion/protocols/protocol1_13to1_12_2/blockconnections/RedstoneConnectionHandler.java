package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedstoneConnectionHandler implements ConnectionHandler {

    private static HashSet<String> baseRedstone = new HashSet<>();
    private static Set<Integer> redstone = new HashSet<>();


    static void init() {
        baseRedstone.add("minecraft:redstone_wire");

        RedstoneConnectionHandler connectionHandler = new RedstoneConnectionHandler();
        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            if (baseRedstone.contains(key)) {
                redstone.add(blockState.getValue());
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
            }
        }
    }


    @Override
    public int connect(Position position, int blockState, ConnectionData connectionData) {
        WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
        blockdata.set("east", connects(position, BlockFace.EAST, connectionData));
        blockdata.set("north", connects(position, BlockFace.NORTH, connectionData));
        blockdata.set("south", connects(position, BlockFace.SOUTH, connectionData));
        blockdata.set("west", connects(position, BlockFace.WEST, connectionData));
        return blockdata.getBlockStateId();
    }

    private String connects(Position position, BlockFace side, ConnectionData connectionData) {
        int blockState = connectionData.get(position.getRelative(side));
        if(redstone.contains(blockState)){
            return "side";
        }
        int up = connectionData.get(position.getRelative(side).getRelative(BlockFace.TOP));
        if(redstone.contains(up)){
            return "up";
        }
        int down = connectionData.get(position.getRelative(side).getRelative(BlockFace.BOTTOM));
        if(redstone.contains(down)){
            return "side";
        }
        return "none";
    }
}
