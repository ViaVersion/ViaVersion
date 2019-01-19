package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

public class TripwireConnectionHandler extends ConnectionHandler {
    private static Map<Integer, TripwireData> tripwireDataMap = new HashMap<>();
    private static Map<Byte, Integer> connectedBlocks = new HashMap<>();
    private static Map<Integer, BlockFace> tripwireHooks = new HashMap<>();

    static ConnectionData.ConnectorInitAction init() {
        final TripwireConnectionHandler connectionHandler = new TripwireConnectionHandler();
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                if (blockData.getMinecraftKey().equals("minecraft:tripwire_hook")) {
                    tripwireHooks.put(blockData.getSavedBlockStateId(), BlockFace.valueOf(blockData.getValue("facing").toUpperCase()));
                } else if (blockData.getMinecraftKey().equals("minecraft:tripwire")) {
                    TripwireData tripwireData = new TripwireData(
                            blockData.getValue("attached").equals("true"),
                            blockData.getValue("disarmed").equals("true"),
                            blockData.getValue("powered").equals("true")
                    );

                    tripwireDataMap.put(blockData.getSavedBlockStateId(), tripwireData);
                    connectedBlocks.put(getStates(blockData), blockData.getSavedBlockStateId());

                    ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
                }
            }
        };
    }

    private static byte getStates(WrappedBlockData blockData) {
        byte b = 0;
        if (blockData.getValue("attached").equals("true")) b |= 1;
        if (blockData.getValue("disarmed").equals("true")) b |= 2;
        if (blockData.getValue("powered").equals("true")) b |= 4;
        if (blockData.getValue("east").equals("true")) b |= 8;
        if (blockData.getValue("north").equals("true")) b |= 16;
        if (blockData.getValue("south").equals("true")) b |= 32;
        if (blockData.getValue("west").equals("true")) b |= 64;
        return b;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        TripwireData tripwireData = tripwireDataMap.get(blockState);
        if (tripwireData == null) return blockState;
        byte b = 0;
        if (tripwireData.isAttached()) b |= 1;
        if (tripwireData.isDisarmed()) b |= 2;
        if (tripwireData.isPowered()) b |= 4;

        int east = getBlockData(user, position.getRelative(BlockFace.EAST));
        int north = getBlockData(user, position.getRelative(BlockFace.NORTH));
        int south = getBlockData(user, position.getRelative(BlockFace.SOUTH));
        int west = getBlockData(user, position.getRelative(BlockFace.WEST));

        if (tripwireDataMap.containsKey(east) || tripwireHooks.get(east) == BlockFace.WEST) {
            b |= 8;
        }
        if (tripwireDataMap.containsKey(north) || tripwireHooks.get(north) == BlockFace.SOUTH) {
            b |= 16;
        }
        if (tripwireDataMap.containsKey(south) || tripwireHooks.get(south) == BlockFace.NORTH) {
            b |= 32;
        }
        if (tripwireDataMap.containsKey(west) || tripwireHooks.get(west) == BlockFace.EAST) {
            b |= 64;
        }

        Integer newBlockState = connectedBlocks.get(b);
        return newBlockState == null ? blockState : newBlockState;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class TripwireData {
        private final boolean attached, disarmed, powered;
    }
}
