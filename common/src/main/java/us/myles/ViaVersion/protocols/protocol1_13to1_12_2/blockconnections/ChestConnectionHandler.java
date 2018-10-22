package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

class ChestConnectionHandler implements ConnectionHandler {
	private static Map<Integer, BlockFace> chests = new HashMap<>();
	private static Map<Integer, String> chestType = new HashMap<>();

	static void init() {
		ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
		    String key = blockState.getKey().split("\\[")[0];
			if (key.equals("minecraft:chest") || key.equals("minecraft:trapped_chest")) {
				String facing = blockState.getKey().substring(key.equals("minecraft:trapped_chest") ? 31 :  23 );
				facing = facing.substring(0, facing.indexOf(','));
				facing = facing.toUpperCase();
				chests.put(blockState.getValue(), BlockFace.valueOf(facing));
				chestType.put(blockState.getValue(), key);
				ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
			}
		}
	}

	@Override
	public int connect(Position position, int blockState, ConnectionData connectionData) {
		BlockFace facing = chests.get(blockState);
		WrappedBlockdata blockdata = WrappedBlockdata.fromStateId(blockState);
		String type = "single";
		if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.NORTH))) && chestType.get(blockState).equals(chestType.get(connectionData.get(position.getRelative(BlockFace.NORTH))))) {
			type = facing == BlockFace.WEST ? "left" : "right";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.SOUTH))) && chestType.get(blockState).equals(chestType.get(connectionData.get(position.getRelative(BlockFace.SOUTH))))) {
			type = facing == BlockFace.EAST ? "left" : "right";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.WEST))) && chestType.get(blockState).equals(chestType.get(connectionData.get(position.getRelative(BlockFace.WEST))))) {
			type = facing == BlockFace.NORTH ? "right" : "left";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.EAST))) && chestType.get(blockState).equals(chestType.get(connectionData.get(position.getRelative(BlockFace.EAST))))) {
			type = facing == BlockFace.SOUTH ? "right" : "left";
		}
        blockdata.set("type", type);
		return blockdata.getBlockStateId();
	}
}
