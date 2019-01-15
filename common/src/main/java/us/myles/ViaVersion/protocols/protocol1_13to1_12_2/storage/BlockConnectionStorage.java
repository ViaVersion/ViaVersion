package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
    private Map<Long, Map<Long, Short>> blockStorage = createLongObjectMap();

    private static Class<?> fastUtilLongObjectHashMap;
    private static Class<?> fastUtilLongShortHashMap;
    private static Class<?> nettyLongObjectHashMap;

    static {
        try {
            fastUtilLongObjectHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
            Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            fastUtilLongShortHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap");
            Via.getPlatform().getLogger().info("Using FastUtil Long2ShortOpenHashMap for block connections");
        } catch (ClassNotFoundException ignored) {
        }
        if (fastUtilLongShortHashMap == null && fastUtilLongObjectHashMap == null) {
            try {
                nettyLongObjectHashMap = Class.forName("io.netty.util.collection.LongObjectHashMap");
                Via.getPlatform().getLogger().info("Using Netty LongObjectHashMap for block connections");
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    public BlockConnectionStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        long pair = getChunkIndex(position);
        Map<Long, Short> map = getChunkMap(pair);
        map.put(encodeBlockPos(position), (short) blockState);
    }

    public int get(Position position) {
        long pair = getChunkIndex(position);
        Map<Long, Short> map = getChunkMap(pair);
        long blockPositon = encodeBlockPos(position);
        return map.containsKey(blockPositon) ? map.get(blockPositon) : 0;
    }

    public void remove(Position position) {
        long pair = getChunkIndex(position);
        Map<Long, Short> map = getChunkMap(pair);
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

    private Map<Long, Short> getChunkMap(long index) {
        Map<Long, Short> map = blockStorage.get(index);
        if (map == null) {
            map = createLongShortMap();
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

    private long encodeBlockPos(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF);
    }

    private long encodeBlockPos(Position pos) {
        return encodeBlockPos(pos.getX().intValue(), pos.getY().intValue(), pos.getZ().intValue());
    }

    private <T> Map<Long, T> createLongObjectMap() {
        if (fastUtilLongObjectHashMap != null) {
            try {
                return (Map<Long, T>) fastUtilLongObjectHashMap.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (nettyLongObjectHashMap != null) {
            try {
                return (Map<Long, T>) nettyLongObjectHashMap.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private Map<Long, Short> createLongShortMap() {
        if (fastUtilLongShortHashMap != null) {
            try {
                return (Map<Long, Short>) fastUtilLongShortHashMap.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (fastUtilLongObjectHashMap != null) {
            try {
                return (Map<Long, Short>) fastUtilLongObjectHashMap.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (nettyLongObjectHashMap != null) {
            try {
                return (Map<Long, Short>) nettyLongObjectHashMap.getConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }
}
