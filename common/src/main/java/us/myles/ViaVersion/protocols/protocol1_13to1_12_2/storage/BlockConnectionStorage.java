package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
    private Map<Long, Map<Short, Short>> blockStorage = createLongObjectMap();

    private static Constructor<?> fastUtilLongObjectHashMap;
    private static Constructor<?> fastUtilShortShortHashMap;

    static {
        try {
            fastUtilLongObjectHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {

        }
        try {
            fastUtilShortShortHashMap = Class.forName("it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap").getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil Short2ShortOpenHashMap for block connections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    public BlockConnectionStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        long pair = getChunkIndex(position);
        Map<Short, Short> map = getChunkMap(pair);
        map.put(encodeBlockPos(position), (short) blockState);
    }

    public int get(Position position) {
        long pair = getChunkIndex(position);
        Map<Short, Short> map = getChunkMap(pair);
        short blockPosition = encodeBlockPos(position);
        return map.containsKey(blockPosition) ? map.get(blockPosition) : 0;
    }

    public void remove(Position position) {
        long pair = getChunkIndex(position);
        Map<Short, Short> map = getChunkMap(pair);
        map.remove(encodeBlockPos(position));
        if (map.isEmpty()) {
            blockStorage.remove(pair);
        }
    }

    public void clear() {
        blockStorage.clear();
    }

    public void unloadChunk(int x, int z) {
        blockStorage.remove(getChunkIndex(x, z));
    }

    private Map<Short, Short> getChunkMap(long index) {
        Map<Short, Short> map = blockStorage.get(index);
        if (map == null) {
            map = createShortShortMap();
            blockStorage.put(index, map);
        }
        return map;
    }

    private long getChunkIndex(int x, int z) {
        return (long) x << 32 | (z & 0xFFFFFFFFL);
    }

    private long getChunkIndex(Position position) {
        return getChunkIndex(position.getX().intValue(), position.getZ().intValue());
    }

    private short encodeBlockPos(int x, int y, int z) {
        return (short) (y << 8 | x & 0xF << 4 | z & 0xF);
    }

    private short encodeBlockPos(Position pos) {
        return encodeBlockPos(pos.getX().intValue(), pos.getY().intValue(), pos.getZ().intValue());
    }

    private <T> Map<Long, T> createLongObjectMap() {
        if (fastUtilLongObjectHashMap != null) {
            try {
                return (Map<Long, T>) fastUtilLongObjectHashMap.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private Map<Short, Short> createShortShortMap() {
        if (fastUtilShortShortHashMap != null) {
            try {
                return (Map<Short, Short>) fastUtilShortShortHashMap.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }
}
