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
		blockState = ConnectionData.connect(position, blockState, this);
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
							block = ConnectionData.connect(new Position(xOff+ x, yOff + y, zOff + z), block, this);
							section.setFlatBlock(x, y, z, block);
							store(xOff + x, yOff + y, zOff + z, block);
						}

						if (x == 0) {
							update(new Position(xOff - 1, yOff + y, zOff + z));
						} else if (x == 15) {
							update(new Position(xOff + 16, yOff + y, zOff + z));
						}
						if (z == 0) {
							update(new Position(xOff + x, yOff + y, zOff - 1));
						} else if (z == 15) {
							update(new Position(xOff + x, yOff + y, zOff + 16));
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
		ChestConnectionHandler.init();
	}

	public static boolean isWelcome(int blockState) {
		return solidBlocks.containsKey(blockState) || connectionHandlerMap.containsKey(blockState);
	}

	public static boolean connects(int blockState) {
		return connectionHandlerMap.containsKey(blockState);
	}

	public static int connect(Position position, int blockState, ConnectionData connectionData) {
		if (connectionHandlerMap.containsKey(blockState)) {
			return connectionHandlerMap.get(blockState).connect(position, blockState, connectionData);
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

	private static Position getRelative(Position position, BlockFace face) {
		switch (face) {
			case NORTH:
				return new Position(position.getX(), position.getY(), position.getZ() - 1);
			case SOUTH:
				return new Position(position.getX(), position.getY(), position.getZ() + 1);
			case EAST:
				return new Position(position.getX() + 1, position.getY(), position.getZ());
			case WEST:
				return new Position(position.getX() - 1, position.getY(), position.getZ());
			case TOP:
				return new Position(position.getX(), position.getY() + 1, position.getZ() - 1);
			case BOTTOM:
				return new Position(position.getX(), position.getY() - 1, position.getZ() - 1);
			default:
				return position;
		}
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
		public int connect(Position position, int blockState, ConnectionData connectionData);
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

			FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
			for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
				String key = blockState.getKey().split("\\[")[0];
				if (baseFences.contains(key)) {
					fences.put(blockState.getValue(), key);
					connectionHandlerMap.put(blockState.getValue(), connectionHandler);
				}
			}
		}

		@Override
		public int connect(Position position, int blockState, ConnectionData connectionData) {
			String key = fences.get(blockState) + '[' +
					             "east=" + connects(BlockFace.EAST, connectionData.get(getRelative(position, BlockFace.EAST))) + ',' +
					             "north=" + connects(BlockFace.NORTH, connectionData.get(getRelative(position, BlockFace.NORTH))) + ',' +
					             "south=" + connects(BlockFace.SOUTH, connectionData.get(getRelative(position, BlockFace.SOUTH))) + ',' +
					             "waterlogged=false," +
					             "west=" + connects(BlockFace.WEST, connectionData.get(getRelative(position, BlockFace.WEST))) +
					             ']';
			return getId(key);
		}

		private boolean connects(BlockFace side, int blockState) {
			return fences.containsKey(blockState) || solidBlocks.containsKey(blockState) && solidBlocks.get(blockState).contains(side.opposite());
		}
	}

    private static class GlassConnectionHandler implements ConnectionHandler {
        private static HashSet<String> basePanes = new HashSet<>();
        private static Map<Integer, String> panes = new HashMap<>();

        private static void init() {
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
            for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
                String key = blockState.getKey().split("\\[")[0];
                if (basePanes.contains(key)) {
                    panes.put(blockState.getValue(), key);
	                connectionHandlerMap.put(blockState.getValue(), connectionHandler);
                }
            }
        }

        @Override
        public int connect(Position position, int blockState, ConnectionData connectionData) {
            String key = panes.get(blockState) + '[' +
		                         "east=" + connects(BlockFace.EAST, connectionData.get(getRelative(position, BlockFace.EAST))) + ',' +
		                         "north=" + connects(BlockFace.NORTH, connectionData.get(getRelative(position, BlockFace.NORTH))) + ',' +
		                         "south=" + connects(BlockFace.SOUTH, connectionData.get(getRelative(position, BlockFace.SOUTH))) + ',' +
		                         "waterlogged=false," +
		                         "west=" + connects(BlockFace.WEST, connectionData.get(getRelative(position, BlockFace.WEST))) +
		                         ']';
            return getId(key);
        }

        private boolean connects(BlockFace side, int blockState) {
            return panes.containsKey(blockState) || solidBlocks.containsKey(blockState) && solidBlocks.get(blockState).contains(side.opposite());
        }
    }

    private static class ChestConnectionHandler implements ConnectionHandler {
        private static Map<Integer, BlockFace> chests = new HashMap<>();

        private static void init() {
	        ChestConnectionHandler connectionHandler = new ChestConnectionHandler();
            for (Map.Entry<String, Integer> blockState : keyToId.entrySet()) {
                if (blockState.getKey().startsWith("minecraft:chest")) {
                	String facing = blockState.getKey().substring(23);
                	facing = facing.substring(0, facing.indexOf(','));
                	facing = facing.toUpperCase();
                    chests.put(blockState.getValue(), BlockFace.valueOf(facing));
	                connectionHandlerMap.put(blockState.getValue(), connectionHandler);
                }
            }
        }

        @Override
        public int connect(Position position, int blockState, ConnectionData connectionData) {
        	BlockFace facing = chests.get(blockState);
        	String type = "single";
        	if (chests.containsKey(connectionData.get(getRelative(position, BlockFace.NORTH)))) {
				type = facing == BlockFace.WEST ? "right" : "left";
	        } else if (chests.containsKey(connectionData.get(getRelative(position, BlockFace.SOUTH)))) {
		        type = facing == BlockFace.EAST ? "right" : "left";
	        } else if (chests.containsKey(connectionData.get(getRelative(position, BlockFace.WEST)))) {
		        type = facing == BlockFace.NORTH ? "right" : "left";
	        } else if (chests.containsKey(connectionData.get(getRelative(position, BlockFace.EAST)))) {
		        type = facing == BlockFace.SOUTH ? "right" : "left";
	        }

            String key = "minecraft:chest" + '[' +
		                         "facing=" + facing.name().toLowerCase() + ',' +
		                         "type=" + type + ',' +
		                         "waterlogged=false" +
		                         ']';
            return getId(key);
        }
    }
}
