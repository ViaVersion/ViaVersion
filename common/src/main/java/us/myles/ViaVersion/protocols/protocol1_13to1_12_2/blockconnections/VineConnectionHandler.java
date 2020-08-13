package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Set;

class VineConnectionHandler extends ConnectionHandler {
    private static final Set<Integer> vines = new HashSet<>();

    static ConnectionData.ConnectorInitAction init() {
        final VineConnectionHandler connectionHandler = new VineConnectionHandler();
        return blockData -> {
            if (!blockData.getMinecraftKey().equals("minecraft:vine")) return;

            vines.add(blockData.getSavedBlockStateId());
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
        };
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        if (isAttachedToBlock(user, position)) return blockState;

        Position upperPos = position.getRelative(BlockFace.TOP);
        int upperBlock = getBlockData(user, upperPos);
        if (vines.contains(upperBlock) && isAttachedToBlock(user, upperPos)) return blockState;

        // Map to air if not attached to block, and upper block is also not a vine attached to a block
        return 0;
    }

    private boolean isAttachedToBlock(UserConnection user, Position position) {
        return isAttachedToBlock(user, position, BlockFace.EAST)
                || isAttachedToBlock(user, position, BlockFace.WEST)
                || isAttachedToBlock(user, position, BlockFace.NORTH)
                || isAttachedToBlock(user, position, BlockFace.SOUTH);
    }

    private boolean isAttachedToBlock(UserConnection user, Position position, BlockFace blockFace) {
        return ConnectionData.occludingStates.contains(getBlockData(user, position.getRelative(blockFace)));
    }
}
