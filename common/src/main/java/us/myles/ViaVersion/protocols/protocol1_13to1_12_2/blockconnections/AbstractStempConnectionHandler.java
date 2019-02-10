package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbstractStempConnectionHandler extends ConnectionHandler {
    private static final BlockFace[] BLOCK_FACES = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};

    private int baseStateId;
    private Set<Integer> blockId = new HashSet<>();

    private Map<BlockFace, Integer> stemps = new HashMap<>();

    public AbstractStempConnectionHandler(String baseStateId) {
        this.baseStateId = ConnectionData.getId(baseStateId);
    }

    public ConnectionData.ConnectorInitAction getInitAction(final String blockId, final String toKey) {
        final AbstractStempConnectionHandler handler = this;
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                if (blockData.getSavedBlockStateId() == baseStateId || blockId.equals(blockData.getMinecraftKey())) {
                    if (blockData.getSavedBlockStateId() != baseStateId) {
                        handler.blockId.add(blockData.getSavedBlockStateId());
                    }
                    ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                }
                if (blockData.getMinecraftKey().equals(toKey)) {
                    String facing = blockData.getValue("facing").toUpperCase();
                    stemps.put(BlockFace.valueOf(facing), blockData.getSavedBlockStateId());
                }
            }
        };
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        if (blockState != baseStateId) {
            return blockState;
        }
        for (BlockFace blockFace : BLOCK_FACES) {
            if (blockId.contains(getBlockData(user, position.getRelative(blockFace)))) {
                return stemps.get(blockFace);
            }
        }
        return baseStateId;
    }
}
