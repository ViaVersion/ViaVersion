package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.Getter;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractFenceConnectionHandler implements ConnectionHandler{

    @Getter
    private List<String> keyList;
    private String blockConnections;
    @Getter
    private HashSet<Integer> blockStates = new HashSet<>();

    public AbstractFenceConnectionHandler(String blockConnections, List<String> keyList){
        this.blockConnections = blockConnections;
        this.keyList = keyList;

        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            if (keyList.contains(key)) {
                blockStates.add(blockState.getValue());
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), this);
            }
        }
    }

    @Override
    public int connect(Position position, int blockState, ConnectionData connectionData) {
        WrappedBlockdata blockdata = WrappedBlockdata.fromStateId(blockState);
        onConnect(position, blockState, connectionData, blockdata);
        blockdata.set("east", connects(BlockFace.EAST, connectionData.get(position.getRelative(BlockFace.EAST))));
        blockdata.set("north", connects(BlockFace.NORTH, connectionData.get(position.getRelative(BlockFace.NORTH))));
        blockdata.set("south", connects(BlockFace.SOUTH, connectionData.get(position.getRelative(BlockFace.SOUTH))));
        blockdata.set("west", connects(BlockFace.WEST, connectionData.get(position.getRelative(BlockFace.WEST))));
        return blockdata.getBlockStateId();
    }

    public abstract void onConnect(Position position, int blockState, ConnectionData connectionData, WrappedBlockdata blockdata);


    private boolean connects(BlockFace side, int blockState) {
        return blockStates.contains(blockState) || ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectTo(blockConnections, side.opposite());
    }
}
