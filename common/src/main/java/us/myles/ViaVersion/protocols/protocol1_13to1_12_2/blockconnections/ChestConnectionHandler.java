package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.*;

class ChestConnectionHandler extends ConnectionHandler {
    private static final Map<Integer, BlockFace> chestFacings = new HashMap<>();
    private static final Map<Byte, Integer> connectedStates = new HashMap<>();
    private static final Set<Integer> trappedChests = new HashSet<>();

    static ConnectionData.ConnectorInitAction init() {
        final ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
        return blockData -> {
            if (!blockData.getMinecraftKey().equals("minecraft:chest") && !blockData.getMinecraftKey().equals("minecraft:trapped_chest"))
                return;
            if (blockData.getValue("waterlogged").equals("true")) return;
            chestFacings.put(blockData.getSavedBlockStateId(), BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)));
            if (blockData.getMinecraftKey().equalsIgnoreCase("minecraft:trapped_chest")) {
                trappedChests.add(blockData.getSavedBlockStateId());
            }
            connectedStates.put(getStates(blockData), blockData.getSavedBlockStateId());
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
        };
    }

    private static Byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        String type = blockData.getValue("type");
        if (type.equals("left")) states |= 1;
        if (type.equals("right")) states |= 2;
        states |= (BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)).ordinal() << 2);
        if (blockData.getMinecraftKey().equals("minecraft:trapped_chest")) states |= 16;
        return states;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        BlockFace facing = chestFacings.get(blockState);
        byte states = 0;
        states |= (facing.ordinal() << 2);
        boolean trapped = trappedChests.contains(blockState);
        if (trapped) states |= 16;
        int relative;
        if (chestFacings.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.NORTH))) && trapped == trappedChests.contains(relative)) {
            states |= facing == BlockFace.WEST ? 1 : 2;
        } else if (chestFacings.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.SOUTH))) && trapped == trappedChests.contains(relative)) {
            states |= facing == BlockFace.EAST ? 1 : 2;
        } else if (chestFacings.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.WEST))) && trapped == trappedChests.contains(relative)) {
            states |= facing == BlockFace.NORTH ? 2 : 1;
        } else if (chestFacings.containsKey(relative = getBlockData(user, position.getRelative(BlockFace.EAST))) && trapped == trappedChests.contains(relative)) {
            states |= facing == BlockFace.SOUTH ? 2 : 1;
        }
        Integer newBlockState = connectedStates.get(states);
        return newBlockState == null ? blockState : newBlockState;
    }
}
