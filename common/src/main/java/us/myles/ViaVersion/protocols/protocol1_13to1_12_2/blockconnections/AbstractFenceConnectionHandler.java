package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.Getter;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFenceConnectionHandler extends ConnectionHandler {
    private final String blockConnections;
    @Getter
    private Set<Integer> blockStates = new HashSet<>();
    private Map<Byte, Integer> connectedBlockStates = new HashMap<>();
    private static final StairConnectionHandler STAIR_CONNECTION_HANDLER = new StairConnectionHandler();

    public AbstractFenceConnectionHandler(String blockConnections) {
        this.blockConnections = blockConnections;
    }

    public ConnectionData.ConnectorInitAction getInitAction(final String key) {
        final AbstractFenceConnectionHandler handler = this;
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                if (key.equals(blockData.getMinecraftKey())) {
                    if (blockData.hasData("waterlogged") && blockData.getValue("waterlogged").equals("true")) return;
                    blockStates.add(blockData.getSavedBlockStateId());
                    ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                    connectedBlockStates.put(getStates(blockData), blockData.getSavedBlockStateId());
                }
            }
        };
    }

    protected byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        if (blockData.getValue("east").equals("true")) states |= 1;
        if (blockData.getValue("north").equals("true")) states |= 2;
        if (blockData.getValue("south").equals("true")) states |= 4;
        if (blockData.getValue("west").equals("true")) states |= 8;
        return states;
    }

    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = 0;
        if (connects(BlockFace.EAST, getBlockData(user, position.getRelative(BlockFace.EAST)))) states |= 1;
        if (connects(BlockFace.NORTH, getBlockData(user, position.getRelative(BlockFace.NORTH)))) states |= 2;
        if (connects(BlockFace.SOUTH, getBlockData(user, position.getRelative(BlockFace.SOUTH)))) states |= 4;
        if (connects(BlockFace.WEST, getBlockData(user, position.getRelative(BlockFace.WEST)))) states |= 8;
        return states;
    }

    @Override
    public int getBlockData(UserConnection user, Position position) {
        return STAIR_CONNECTION_HANDLER.connect(user, position, super.getBlockData(user, position));
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        final Integer newBlockState = connectedBlockStates.get(getStates(user, position, blockState));
        return newBlockState == null ? blockState : newBlockState;
    }

    protected boolean connects(BlockFace side, int blockState) {
        return blockStates.contains(blockState) || blockConnections != null && ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectsTo(blockConnections, side.opposite());
    }
}
