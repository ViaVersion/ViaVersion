package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class FenceConnectionHandler implements ConnectionHandler {
	private static HashSet<String> baseFences = new HashSet<>();
	private static Map<Integer, String> fences = new HashMap<>();

	static void init() {
		baseFences.add("minecraft:oak_fence");
		baseFences.add("minecraft:birch_fence");
		baseFences.add("minecraft:jungle_fence");
		baseFences.add("minecraft:dark_oak_fence");
		baseFences.add("minecraft:acacia_fence");
		baseFences.add("minecraft:spruce_fence");
		baseFences.add("minecraft:cobblestone_wall");
		baseFences.add("minecraft:mossy_cobblestone_wall");

		FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
			String key = blockState.getKey().split("\\[")[0];
			if (baseFences.contains(key)) {
				fences.put(blockState.getValue(), key);
				ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
			}
		}
	}

	@Override
	public int connect(Position position, int blockState, ConnectionData connectionData) {
	    String key = fences.get(blockState);
	    String extraData = "";
	    if(key.endsWith("_wall")){
	        extraData = "up=" + key.equals(fences.get(connectionData.get(position.getRelative(BlockFace.TOP)))) + ",";
        }
		String data = key + '[' +
				             "east=" + connects(BlockFace.EAST, connectionData.get(position.getRelative(BlockFace.EAST))) + ',' +
				             "north=" + connects(BlockFace.NORTH, connectionData.get(position.getRelative(BlockFace.NORTH))) + ',' +
				             "south=" + connects(BlockFace.SOUTH, connectionData.get(position.getRelative(BlockFace.SOUTH))) + ',' +
                             extraData +
				             "waterlogged=false," +
				             "west=" + connects(BlockFace.WEST, connectionData.get(position.getRelative(BlockFace.WEST))) +
				             ']';
		return ConnectionData.getId(data);
	}

	private boolean connects(BlockFace side, int blockState) {
		return fences.containsKey(blockState) || ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectsToFences(side.opposite());
	}
}
