package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedstoneConnectionHandler extends ConnectionHandler {

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
    public int connect(UserConnection user, Position position, int blockState) {
        WrappedBlockData blockdata = WrappedBlockData.fromStateId(blockState);
        blockdata.set("east", connects(user, position, BlockFace.EAST));
        blockdata.set("north", connects(user, position, BlockFace.NORTH));
        blockdata.set("south", connects(user, position, BlockFace.SOUTH));
        blockdata.set("west", connects(user, position, BlockFace.WEST));
        return blockdata.getBlockStateId();
    }

    private String connects(UserConnection user, Position position, BlockFace side) {
        int blockState = getBlockData(user, position.getRelative(side));
        if(redstone.contains(blockState)){
            return "side";
        }
        int up = getBlockData(user, position.getRelative(side).getRelative(BlockFace.TOP));
        if(redstone.contains(up)){
            return "up";
        }
        int down = getBlockData(user, position.getRelative(side).getRelative(BlockFace.BOTTOM));
        if(redstone.contains(down)){
            return "side";
        }
        return "none";
    }
}
