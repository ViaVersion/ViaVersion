package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
    private Map<Long, short[]> blockStorage = createLongObjectMap();
    private static short[] short4096 = new short[4096];

    private static Constructor<?> fastUtilLongObjectHashMap;

    static {
        try {
            fastUtilLongObjectHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    public BlockConnectionStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        long pair = getChunkSectionIndex(position);
        short[] map = getChunkSection(pair);
        map[encodeBlockPos(position)] = (short) blockState;
    }

    public int get(Position position) {
        long pair = getChunkSectionIndex(position);
        short[] map = blockStorage.get(pair);
        if (map == null) return 0;
        short blockPosition = encodeBlockPos(position);
        return map[blockPosition];
    }

    public void remove(Position position) {
        long pair = getChunkSectionIndex(position);
        short[] map = getChunkSection(pair);
        map[encodeBlockPos(position)] = 0;
        if (Arrays.equals(short4096, map)) {
            blockStorage.remove(pair);
        }
    }

    public void clear() {
        blockStorage.clear();
    }

    public void unloadChunk(int x, int z) {
        for (int y = 0; y < 256; y += 16) {
            blockStorage.remove(getChunkSectionIndex(x, y, z));
        }
    }

    private short[] getChunkSection(long index) {
        short[] map = blockStorage.get(index);
        if (map == null) {
            map = new short[4096];
            blockStorage.put(index, map);
        }
        return map;
    }

    private long getChunkSectionIndex(int x, int y, int z) {
        return (((x >> 4) & 0x3FFFFFFL) << 38) | (((y >> 4) & 0xFFFL) << 26) | ((z >> 4) & 0x3FFFFFFL);
    }

    private long getChunkSectionIndex(Position position) {
        return getChunkSectionIndex(position.getX().intValue(), position.getY().intValue(), position.getZ().intValue());
    }

    private short encodeBlockPos(int x, int y, int z) {
        return (short) (((y & 0xF) << 8) | ((x & 0xF) << 4) | (z & 0xF));
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
}
