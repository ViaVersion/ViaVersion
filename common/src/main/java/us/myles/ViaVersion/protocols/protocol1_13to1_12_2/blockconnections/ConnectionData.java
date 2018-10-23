package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

import java.util.*;
import java.util.Map.Entry;

public class ConnectionData extends StoredObject  {
	static Map<Integer, String> idToKey = new HashMap<>();
	static Map<String, Integer> keyToId = new HashMap<>();
	static Map<Integer, ConnectionHandler> connectionHandlerMap = new HashMap<>();
	static Map<Integer, BlockData> blockConnectionData = new HashMap<>();

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

        JsonObject mappingBlockConnections = MappingData.loadData("blockConnections.json");
        for (Entry<String, JsonElement> entry : mappingBlockConnections.entrySet()) {
            int id = keyToId.get(entry.getKey());
            BlockData blockData = new BlockData();
            for (Entry<String, JsonElement> type : entry.getValue().getAsJsonObject().entrySet()) {
                String name = type.getKey();
                JsonObject object = type.getValue().getAsJsonObject();
                Boolean[] data = new Boolean[6];
                for (BlockFace value : BlockFace.values()) {
                    String face = value.toString().toLowerCase();
                    if(object.has(face)){
                        data[value.ordinal()] = object.getAsJsonPrimitive(face).getAsBoolean();
                    }else{
                        data[value.ordinal()] = false;
                    }
                }
                blockData.put(name, data);
            }
            blockConnectionData.put(id, blockData);
        }

//		FenceConnectionHandler.init();
//		GlassConnectionHandler.init();
        PumpkinConnectionHandler.init();
        MelonConnectionHandler.init();
        BasicFenceConnectionHandler.init();
        NetherFenceConnectionHandler.init();
        WallConnectionHandler.init();
        MelonConnectionHandler.init();
        GlassConnectionHandler.init();
		ChestConnectionHandler.init();
		DoorConnectionHandler.init();
		RedstoneConnectionHandler.init();
		StairConnectionHandler.init();
	}

	public static boolean isWelcome(int blockState) {
		return blockConnectionData.containsKey(blockState) || connectionHandlerMap.containsKey(blockState);
	}

	public static boolean connects(int blockState) {
		return connectionHandlerMap.containsKey(blockState);
	}

	private static int connect(Position position, int blockState, ConnectionData connectionData) {
		if (connectionHandlerMap.containsKey(blockState)) {
		    ConnectionHandler handler = connectionHandlerMap.get(blockState);
			return handler.connect(position, blockState, connectionData);
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
}
