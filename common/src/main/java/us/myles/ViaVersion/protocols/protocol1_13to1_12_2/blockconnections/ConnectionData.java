package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.PacketBlockConnectionProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

import java.util.*;
import java.util.Map.Entry;

public class ConnectionData {
    static Map<Integer, String> idToKey = new HashMap<>();
    static Map<String, Integer> keyToId = new HashMap<>();
    static Map<Integer, ConnectionHandler> connectionHandlerMap = new HashMap<>();
    static Map<Integer, BlockData> blockConnectionData = new HashMap<>();
    static Set<Integer> occludingStates = new HashSet<>();

    public static void update(UserConnection user, Position position) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) continue;
                    Position pos = new Position(position.getX() + x, position.getY() + y, position.getZ() + z);
                    int blockState = Via.getManager().getProviders().get(BlockConnectionProvider.class).getBlockdata(user, pos);
                    if (!connects(blockState)) continue;
                    int newBlockState = connect(user, pos, blockState);
                    if (newBlockState == blockState) continue;

                    PacketWrapper blockUpdatePacket = new PacketWrapper(0x0B, null, user);
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

    public static BlockConnectionProvider getProvider() {
        return Via.getManager().getProviders().get(BlockConnectionProvider.class);
    }

    public static void updateBlockStorage(UserConnection userConnection, Position position, int blockState) {
        if (!needStoreBlocks()) return;
        if (ConnectionData.isWelcome(blockState)) {
            ConnectionData.getProvider().storeBlock(userConnection, position, blockState);
        } else {
            ConnectionData.getProvider().removeBlock(userConnection, position);
        }
    }

    public static void clearBlockStorage(UserConnection connection) {
        if (!needStoreBlocks()) return;
        getProvider().clearStorage(connection);
    }

    public static boolean needStoreBlocks() {
        return getProvider().storesBlocks();
    }

    public static void connectBlocks(UserConnection user, Chunk chunk) {
        long xOff = chunk.getX() << 4;
        long zOff = chunk.getZ() << 4;

        for (int i = 0; i < chunk.getSections().length; i++) {
            ChunkSection section = chunk.getSections()[i];
            if (section == null) continue;

            boolean willConnect = false;

            for (int p = 0; p < section.getPaletteSize(); p++) {
                int id = section.getPaletteEntry(p);
                if (ConnectionData.connects(id)) {
                    willConnect = true;
                    break;
                }
            }
            if (!willConnect) continue;

            long yOff = i << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int block = section.getFlatBlock(x, y, z);

                        if (ConnectionData.connects(block)) {
                            block = ConnectionData.connect(user, new Position(xOff + x, yOff + y, zOff + z), block);
                            section.setFlatBlock(x, y, z, block);
                        }

                        if (x == 0) {
                            update(user, new Position(xOff - 1, yOff + y, zOff + z));
                        } else if (x == 15) {
                            update(user, new Position(xOff + 16, yOff + y, zOff + z));
                        }
                        if (z == 0) {
                            update(user, new Position(xOff + x, yOff + y, zOff - 1));
                        } else if (z == 15) {
                            update(user, new Position(xOff + x, yOff + y, zOff + 16));
                        }
                    }
                }
            }
        }
    }

    public static void init() {
        if (!Via.getConfig().isServersideBlockConnections()) return;
        Via.getPlatform().getLogger().info("Loading block connection mappings ...");
        JsonObject mapping1_13 = MappingData.loadData("mapping-1.13.json");
        JsonObject blocks1_13 = mapping1_13.getAsJsonObject("blocks");
        for (Entry<String, JsonElement> blockState : blocks1_13.entrySet()) {
            Integer id = Integer.parseInt(blockState.getKey());
            String key = blockState.getValue().getAsString();
            idToKey.put(id, key);
            keyToId.put(key, id);
        }

        if (!Via.getConfig().isReduceBlockStorageMemory()) {
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
                        if (object.has(face)) {
                            data[value.ordinal()] = object.getAsJsonPrimitive(face).getAsBoolean();
                        } else {
                            data[value.ordinal()] = false;
                        }
                    }
                    blockData.put(name, data);
                }
                blockConnectionData.put(id, blockData);
            }
        }

        JsonObject blockData = MappingData.loadData("blockData.json");
        JsonArray occluding = blockData.getAsJsonArray("occluding");
        for (JsonElement jsonElement : occluding) {
            occludingStates.add(keyToId.get(jsonElement.getAsString()));
        }

        List<ConnectorInitAction> initActions = new ArrayList<>();
        initActions.add(PumpkinConnectionHandler.init());
        initActions.addAll(BasicFenceConnectionHandler.init());
        initActions.add(NetherFenceConnectionHandler.init());
        initActions.addAll(WallConnectionHandler.init());
        initActions.add(MelonConnectionHandler.init());
        initActions.addAll(GlassConnectionHandler.init());
        initActions.add(ChestConnectionHandler.init());
        initActions.add(DoorConnectionHandler.init());
        initActions.add(RedstoneConnectionHandler.init());
        initActions.add(StairConnectionHandler.init());
        initActions.add(FlowerConnectionHandler.init());
        initActions.addAll(ChorusPlantConnectionHandler.init());
        initActions.add(TripwireConnectionHandler.init());
        initActions.add(SnowyGrassConnectionHandler.init());
        for (String key : keyToId.keySet()) {
            WrappedBlockData wrappedBlockData = WrappedBlockData.fromString(key);
            for (ConnectorInitAction action : initActions) {
                action.check(wrappedBlockData);
            }
        }

        if (Via.getConfig().getBlockConnectionMethod().equalsIgnoreCase("packet")) {
            Via.getManager().getProviders().register(BlockConnectionProvider.class, new PacketBlockConnectionProvider());
        }
    }

    public static boolean isWelcome(int blockState) {
        return blockConnectionData.containsKey(blockState) || connectionHandlerMap.containsKey(blockState);
    }

    public static boolean connects(int blockState) {
        return connectionHandlerMap.containsKey(blockState);
    }

    public static int connect(UserConnection user, Position position, int blockState) {
        if (connectionHandlerMap.containsKey(blockState)) {
            ConnectionHandler handler = connectionHandlerMap.get(blockState);
            return handler.connect(user, position, blockState);
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

    interface ConnectorInitAction {

        void check(WrappedBlockData blockData);
    }
}
