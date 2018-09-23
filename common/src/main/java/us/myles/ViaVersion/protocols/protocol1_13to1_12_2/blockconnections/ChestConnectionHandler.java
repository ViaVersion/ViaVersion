package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

class ChestConnectionHandler implements ConnectionHandler {
	private static Map<Integer, BlockFace> chests = new HashMap<>();

	static void init() {
		ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
			if (blockState.getKey().startsWith("minecraft:chest")) {
				String facing = blockState.getKey().substring(23);
				facing = facing.substring(0, facing.indexOf(','));
				facing = facing.toUpperCase();
				chests.put(blockState.getValue(), BlockFace.valueOf(facing));
				ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
			}
		}
	}

	@Override
	public int connect(Position position, int blockState, ConnectionData connectionData) {
		BlockFace facing = chests.get(blockState);
		String type = "single";
		if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.NORTH)))) {
			type = facing == BlockFace.WEST ? "left" : "right";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.SOUTH)))) {
			type = facing == BlockFace.EAST ? "left" : "right";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.WEST)))) {
			type = facing == BlockFace.NORTH ? "right" : "left";
		} else if (chests.containsKey(connectionData.get(position.getRelative(BlockFace.EAST)))) {
			type = facing == BlockFace.SOUTH ? "right" : "left";
		}

		String key = "minecraft:chest" + '[' + "facing=" + facing.name().toLowerCase() + ',' + "type=" + type + ',' + "waterlogged=false" + ']';
		return ConnectionData.getId(key);
	}
}
