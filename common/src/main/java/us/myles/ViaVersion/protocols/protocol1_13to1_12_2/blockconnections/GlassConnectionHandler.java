package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class GlassConnectionHandler implements ConnectionHandler {
	private static HashSet<String> basePanes = new HashSet<>();
	private static Map<Integer, String> panes = new HashMap<>();

	static void init() {
		basePanes.add("minecraft:white_stained_glass_pane");
		basePanes.add("minecraft:orange_stained_glass_pane");
		basePanes.add("minecraft:magenta_stained_glass_pane");
		basePanes.add("minecraft:light_blue_stained_glass_pane");
		basePanes.add("minecraft:yellow_stained_glass_pane");
		basePanes.add("minecraft:lime_stained_glass_pane");

		basePanes.add("minecraft:pink_stained_glass_pane");
		basePanes.add("minecraft:gray_stained_glass_pane");
		basePanes.add("minecraft:light_gray_stained_glass_pane");
		basePanes.add("minecraft:cyan_stained_glass_pane");
		basePanes.add("minecraft:purple_stained_glass_pane");
		basePanes.add("minecraft:blue_stained_glass_pane");

		basePanes.add("minecraft:brown_stained_glass_pane");
		basePanes.add("minecraft:green_stained_glass_pane");
		basePanes.add("minecraft:red_stained_glass_pane");
		basePanes.add("minecraft:black_stained_glass_pane");
		basePanes.add("minecraft:glass_pane");
		basePanes.add("minecraft:iron_bars");

		GlassConnectionHandler connectionHandler = new GlassConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
			String key = blockState.getKey().split("\\[")[0];
			if (basePanes.contains(key)) {
				panes.put(blockState.getValue(), key);
				ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
			}
		}
	}

	@Override
	public int connect(Position position, int blockState, ConnectionData connectionData) {
		String key = panes.get(blockState) + '[' + "east=" + connects(BlockFace.EAST, connectionData.get(position.getRelative(BlockFace.EAST))) + ',' + "north=" + connects(BlockFace.NORTH, connectionData.get(position.getRelative(BlockFace.NORTH))) + ',' + "south=" + connects(BlockFace.SOUTH, connectionData
				                                                                                                                                                                                                                                                                                             .get(position.getRelative(BlockFace.SOUTH))) + ',' + "waterlogged=false," + "west=" + connects(BlockFace.WEST, connectionData
						                                                                                                                                                                                                                                                                                                                                                                                                            .get(position.getRelative(BlockFace.WEST))) + ']';
		return ConnectionData.getId(key);
	}

	private boolean connects(BlockFace side, int blockState) {
		return panes.containsKey(blockState) || ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectsToPanes(side.opposite());
	}
}
