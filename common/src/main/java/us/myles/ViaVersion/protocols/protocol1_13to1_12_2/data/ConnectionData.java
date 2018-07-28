package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectionData extends StoredObject  {
	private static Map<Integer, String> idToKey = new HashMap<>();
	private static Map<String, Integer> keyToId = new HashMap<>();
	private static Map<Integer, ConnectionHandler> connectionHandlerMap = new HashMap<>();
	private static Map<Integer, Set<BlockFace>> solidBlocks = new HashMap<>();

	private Map<Position, Integer> blockStorage = new HashMap<>();

	public ConnectionData(UserConnection user) {
		super(user);
	}

	public void store(Position position, int blockState) {
		if (!isWelcome(blockState)) return;
		blockStorage.put(position, blockState);
	}

	public void store(long x, long y, long z, int blockState) {
		store(new Position(x, y, z), blockState);
	}

	public int get(Position position) {
		return blockStorage.containsKey(position) ? blockStorage.get(position) : -1;
	}

	public void remove(Position position) {
		blockStorage.remove(position);
	}

	public int get(long x, long y, long z) {
		return get(new Position(x, y, z));
	}

	public int connect(Position position, int blockState) {
		long x = position.getX();
		long y = position.getY();
		long z = position.getZ();
		blockState = ConnectionData.connect(
				blockState,
				get(x, y, z - 1),
				get(x + 1, y, z),
				get(x, y, z + 1),
				get(x - 1, y, z),
				get(x, y + 1, z),
				get(x, y - 1, z)
		);
		store(position, blockState);
		return blockState;
	}

	public void update(Position position) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				for (int y = -1; y <= 1; y++) {
					if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) continue;
					Position pos = new Position(position.getX() + x, position.getY() + y, position.getZ() + z);
					int blockState = get(pos);
					if (!connects(blockState)) continue;
					int newBlockState = connect(pos, blockState);
					if (newBlockState == blockState) continue;
					store(pos, newBlockState);


					PacketWrapper blockUpdatePacket = new PacketWrapper(0x0B, null, getUser());
					blockUpdatePacket.write(Type.POSITION, pos);
					blockUpdatePacket.write(Type.VAR_INT, newBlockState);
					try {
						blockUpdatePacket.send(Protocol1_13To1_12_2.class, true, false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public void connectBlocks(Chunk chunk) {
		long xOff = chunk.getX() << 4;
		long zOff = chunk.getZ() << 4;

		for (int i = 0; i < chunk.getSections().length; i++) {
			ChunkSection section = chunk.getSections()[i];
			if (section == null) continue;

			long yOff = i << 4;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						int block = section.getBlock(x, y, z);

						if (ConnectionData.connects(block)) {
							block = ConnectionData.connect(
									block,
									get(xOff + x, yOff + y, zOff + z - 1),
									get(xOff + x + 1, yOff + y, zOff + z),
									get(xOff + x, yOff + y, zOff + z + 1),
									get(xOff + x - 1, yOff + y, zOff + z),
									get(xOff + x, yOff + y + 1, zOff + z),
									get(xOff + x, yOff + y - 1, zOff + z)
							);
							section.setFlatBlock(x, y, z, block);
							store(xOff + x, yOff + y, zOff + z, block);
						}
					}
				}
			}
		}
	}

	public static void init() {
		JsonObject mapping1_13 = MappingData.loadData("mapping-1.13.json");
		JsonObject blocks1_13 = mapping1_13.getAsJsonObject("blocks");
		for (Map.Entry<String, JsonElement> blockState : blocks1_13.entrySet()) {
			Integer id = Integer.parseInt(blockState.getKey());
			String key = blockState.getValue().getAsString();
			idToKey.put(id, key);
			keyToId.put(key, id);
		}

		//Temporary
		HashSet<BlockFace> allSides = new HashSet<>(Arrays.asList(BlockFace.values()));

		solidBlocks.put(getId("minecraft:stone"), allSides);
		//

		FenceConnectionHandler.init();
		GlassConnectionHandler.init();
	}

	public static boolean isWelcome(int blockState) {
		return solidBlocks.containsKey(blockState) || connectionHandlerMap.containsKey(blockState);
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

	private enum BlockFace {
		NORTH, SOUTH, EAST, WEST, TOP, BOTTOM;

		private static Map<BlockFace, BlockFace> opposites = new HashMap<>();
		static {
			opposites.put(BlockFace.NORTH, BlockFace.SOUTH);
			opposites.put(BlockFace.SOUTH, BlockFace.NORTH);
			opposites.put(BlockFace.EAST, BlockFace.WEST);
			opposites.put(BlockFace.WEST, BlockFace.EAST);
			opposites.put(BlockFace.TOP, BlockFace.BOTTOM);
			opposites.put(BlockFace.BOTTOM, BlockFace.TOP);
		}

		public BlockFace opposite() {
			return opposites.get(this);
		}
	}

	private interface ConnectionHandler {
		public int connect(int blockState, int north, int east, int south, int west, int top, int bottom);
	}

	private static class FenceConnectionHandler implements ConnectionHandler {
		private static HashSet<String> baseFences = new HashSet<>();
		private static Map<Integer, String> fences = new HashMap<>();

		private static void init() {
			baseFences.add("minecraft:oak_fence");
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
					             "east=" + connects(BlockFace.EAST, east) + ',' +
					             "north=" + connects(BlockFace.NORTH, north) + ',' +
					             "south=" + connects(BlockFace.SOUTH, south) + ',' +
					             "waterlogged=false," +
					             "west=" + connects(BlockFace.WEST, west) +
					             ']';
			return getId(key);
		}

		private boolean connects(BlockFace side, int blockState) {
			return fences.containsKey(blockState) || solidBlocks.containsKey(blockState) && solidBlocks.get(blockState).contains(side.opposite());
		}
	}

    private static class GlassConnectionHandler implements ConnectionHandler {
        private static HashSet<String> baseGlass = new HashSet<>();
        private static Map<Integer, String> glasses = new HashMap<>();

        private static void init() {
            baseGlass.add("minecraft:white_stained_glass_pane");
            baseGlass.add("minecraft:orange_stained_glass_pane");
            baseGlass.add("minecraft:magenta_stained_glass_pane");
            baseGlass.add("minecraft:light_blue_stained_glass_pane");
            baseGlass.add("minecraft:yellow_stained_glass_pane");
            baseGlass.add("minecraft:lime_stained_glass_pane");

            baseGlass.add("minecraft:pink_stained_glass_pane");
            baseGlass.add("minecraft:gray_stained_glass_pane");
            baseGlass.add("minecraft:light_gray_stained_glass_pane");
            baseGlass.add("minecraft:cyan_stained_glass_pane");
            baseGlass.add("minecraft:purple_stained_glass_pane");
            baseGlass.add("minecraft:blue_stained_glass_pane");

            baseGlass.add("minecraft:brown_stained_glass_pane");
            baseGlass.add("minecraft:green_stained_glass_pane");
            baseGlass.add("minecraft:red_stained_glass_pane");
            baseGlass.add("minecraft:black_stained_glass_pane");
            baseGlass.add("minecraft:glass_pane");

            for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
                String key = blockState.getKey().split("\\[")[0];
                if (baseGlass.contains(key)) {
                    glasses.put(blockState.getValue(), key);
                }
            }

            FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
            for (Integer fence : glasses.keySet()) {
                connectionHandlerMap.put(fence, connectionHandler);
            }
        }

        @Override
        public int connect(int blockState, int north, int east, int south, int west, int top, int bottom) {
            String key = glasses.get(blockState) + '[' +
                    "east=" + connects(BlockFace.EAST, east) + ',' +
                    "north=" + connects(BlockFace.NORTH, north) + ',' +
                    "south=" + connects(BlockFace.SOUTH, south) + ',' +
                    "waterlogged=false," +
                    "west=" + connects(BlockFace.WEST, west) +
                    ']';
            return getId(key);
        }

        private boolean connects(BlockFace side, int blockState) {
            return glasses.containsKey(blockState) || solidBlocks.containsKey(blockState) && solidBlocks.get(blockState).contains(side.opposite());
        }
    }

    private static class RedstoneConnectionHandler implements ConnectionHandler {
        private static HashSet<String> baseGlass = new HashSet<>();
        private static Map<Integer, String> glasses = new HashMap<>();

        private static void init() {
            baseGlass.add("minecraft:white_stained_glass_pane");
            baseGlass.add("minecraft:orange_stained_glass_pane");
            baseGlass.add("minecraft:magenta_stained_glass_pane");
            baseGlass.add("minecraft:light_blue_stained_glass_pane");
            baseGlass.add("minecraft:yellow_stained_glass_pane");
            baseGlass.add("minecraft:lime_stained_glass_pane");

            baseGlass.add("minecraft:pink_stained_glass_pane");
            baseGlass.add("minecraft:gray_stained_glass_pane");
            baseGlass.add("minecraft:light_gray_stained_glass_pane");
            baseGlass.add("minecraft:cyan_stained_glass_pane");
            baseGlass.add("minecraft:purple_stained_glass_pane");
            baseGlass.add("minecraft:blue_stained_glass_pane");

            baseGlass.add("minecraft:brown_stained_glass_pane");
            baseGlass.add("minecraft:green_stained_glass_pane");
            baseGlass.add("minecraft:red_stained_glass_pane");
            baseGlass.add("minecraft:black_stained_glass_pane");
            baseGlass.add("minecraft:glass_pane");

            for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
                String key = blockState.getKey().split("\\[")[0];
                if (baseGlass.contains(key)) {
                    glasses.put(blockState.getValue(), key);
                }
            }

            FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
            for (Integer fence : glasses.keySet()) {
                connectionHandlerMap.put(fence, connectionHandler);
            }
        }

        @Override
        public int connect(int blockState, int north, int east, int south, int west, int top, int bottom) {
            String key = glasses.get(blockState) + '[' +
                    "east=" + connects(BlockFace.EAST, east) + ',' +
                    "north=" + connects(BlockFace.NORTH, north) + ',' +
                    "south=" + connects(BlockFace.SOUTH, south) + ',' +
                    "waterlogged=false," +
                    "west=" + connects(BlockFace.WEST, west) +
                    ']';
            return getId(key);
        }

        private boolean connects(BlockFace side, int blockState) {
            return glasses.containsKey(blockState) || solidBlocks.containsKey(blockState) && solidBlocks.get(blockState).contains(side.opposite());
        }
    }
}
