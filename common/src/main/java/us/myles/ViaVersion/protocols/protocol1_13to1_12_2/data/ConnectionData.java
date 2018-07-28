package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConnectionData {
	private static Map<Integer, String> idToKey = new HashMap<>();
	private static Map<String, Integer> keyToId = new HashMap<>();

	private static Map<Integer, ConnectionHandler> connectionHandlerMap = new HashMap<>();

	public static void init() {
		JsonObject mapping1_13 = MappingData.loadData("mapping-1.13.json");
		JsonObject blocks1_13 = mapping1_13.getAsJsonObject("blocks");
		for (Map.Entry<String, JsonElement> blockState : blocks1_13.entrySet()) {
			Integer id = Integer.parseInt(blockState.getKey());
			String key = blockState.getValue().getAsString();
			idToKey.put(id, key);
			keyToId.put(key, id);
		}

		FenceConnectionHandler.init();

	}

	public static boolean connects(int blockState) {
		return connectionHandlerMap.containsKey(blockState);
	}

	public static int connect(int blockState, int north, int east, int south, int west, int top, int bottom) {
		if (connectionHandlerMap.containsKey(blockState)) {
			return connectionHandlerMap.get(blockState).connect(blockState, north, east, south, west, top, bottom);
		} else {
			return blockState;
		}
	}

	public static int getId(String key) {
		return keyToId.containsKey(key) ? keyToId.get(key) : -1;
	}

	public static String getKey(int id) {
		return idToKey.get(id);
	}

	private interface ConnectionHandler {
		public int connect(int blockState, int north, int east, int south, int west, int top, int bottom);
	}

	private static class FenceConnectionHandler implements ConnectionHandler {
		private static HashSet<String> baseFences = new HashSet<>();
		private static Map<Integer, String> fences = new HashMap<>();

		private static void init() {
			baseFences.add("minecraft:oak_fence");
			baseFences.add("minecraft:nether_brick_fence");
			baseFences.add("minecraft:birch_fence");
			baseFences.add("minecraft:jungle_fence");
			baseFences.add("minecraft:dark_oak_fence");
			baseFences.add("minecraft:acacia_fence");
			baseFences.add("minecraft:spruce_fence");

			for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
				String key = blockState.getKey().split("\\[")[0];
				if (baseFences.contains(key)) {
					fences.put(blockState.getValue(), key);
				}
			}

			FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
			for (Integer fence : fences.keySet()) {
				connectionHandlerMap.put(fence, connectionHandler);
			}
		}

		@Override
		public int connect(int blockState, int north, int east, int south, int west, int top, int bottom) {
			String key = fences.get(blockState) + '[' +
					             "east=" + connects("east", east) + ',' +
					             "north=" + connects("north", north) + ',' +
					             "south=" + connects("south", south) + ',' +
					             "waterlogged=false," +
					             "west=" + connects("west", west) +
					             ']';
			return getId(key);
		}

		private boolean connects(String side, int blockState) {
			//TODO solid blocks
			return fences.containsKey(blockState);
		}
	}
}
