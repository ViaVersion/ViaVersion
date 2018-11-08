package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedstoneConnectionHandler extends ConnectionHandler {

    private static Set<Integer> redstone = new HashSet<>();

    static void init() {
        RedstoneConnectionHandler connectionHandler = new RedstoneConnectionHandler();
        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            final String redstoneKey = "minecraft:redstone_wire";
            if (redstoneKey.equals(key)) {
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
        final Position relative = position.getRelative(side);
        int blockState = getBlockData(user, relative);
        if (connects(side, blockState)) {
            return "side";
        }
        int up = getBlockData(user, relative.getRelative(BlockFace.TOP));
        if (redstone.contains(up) && !ConnectionData.occludingStates.contains(getBlockData(user, position.getRelative(BlockFace.TOP)))) {
            return "up";
        }
        int down = getBlockData(user, relative.getRelative(BlockFace.BOTTOM));
        if (redstone.contains(down) && !ConnectionData.occludingStates.contains(getBlockData(user, relative))) {
            return "side";
        }
        return "none";
    }

    private boolean connects(BlockFace side, int blockState) {
        final BlockData blockData = ConnectionData.blockConnectionData.get(blockState);
        return blockData != null && blockData.connectTo("redstoneConnections", side.opposite());
    }
}
