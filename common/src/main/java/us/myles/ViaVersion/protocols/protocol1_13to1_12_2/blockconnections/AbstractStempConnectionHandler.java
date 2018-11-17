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

    public AbstractStempConnectionHandler(String baseStateId, String blockId, String toKey) {
        this.baseStateId = ConnectionData.getId(baseStateId);

        for (Map.Entry<String, Integer> entry : ConnectionData.keyToId.entrySet()) {
            String key = entry.getKey().split("\\[")[0];
            if (entry.getValue() == this.baseStateId || blockId.equals(key)) {
                if (entry.getValue() != this.baseStateId) {
                    this.blockId.add(entry.getValue());
                }
                ConnectionData.connectionHandlerMap.put(entry.getValue(), this);
            }
            if (key.equals(toKey)) {
                WrappedBlockData data = WrappedBlockData.fromString(entry.getKey());
                String facing = data.getValue("facing").toUpperCase();
                stemps.put(BlockFace.valueOf(facing), entry.getValue());
            }
        }
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
