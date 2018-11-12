package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ChestConnectionHandler extends ConnectionHandler {
	private static Map<Integer, BlockFace> chestFacings = new HashMap<>();
	private static Map<Byte, Integer> connectedStates = new HashMap<>();
	private static Set<Integer> trappedChests = new HashSet<>();

	static void init() {
		ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
		    String key = blockState.getKey().split("\\[")[0];
			if (!key.equals("minecraft:chest") && !key.equals("minecraft:trapped_chest")) continue;
			WrappedBlockData blockData = WrappedBlockData.fromString(blockState.getKey());
			if (blockData.getValue("waterlogged").equals("true")) continue;
			chestFacings.put(blockState.getValue(), BlockFace.valueOf(blockData.getValue("facing").toUpperCase()));
			if (key.equalsIgnoreCase("minecraft:trapped_chest")) trappedChests.add(blockState.getValue());
			connectedStates.put(getStates(blockData), blockState.getValue());
			ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
		}
	}

	private static Byte getStates(WrappedBlockData blockData) {
		byte states = 0;
		String type = blockData.getValue("type");
		if (type.equals("left")) states |= 1;
		if (type.equals("right")) states |= 2;
		states |= (BlockFace.valueOf(blockData.getValue("facing").toUpperCase()).ordinal() << 2);
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
