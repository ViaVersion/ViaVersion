package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
    private Map<Long, Pair<byte[], NibbleArray>> blockStorage = createLongObjectMap();

    private static Constructor<?> fastUtilLongObjectHashMap;
    private static HashMap<Short, Short> reverseBlockMappings;

    static {
        try {
            fastUtilLongObjectHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        reverseBlockMappings = new HashMap<>();
        for (int i = 0; i < 4096; i++) {
            int newBlock = MappingData.blockMappings.getNewBlock(i);
            if (newBlock != -1) reverseBlockMappings.put((short) newBlock, (short) i);
        }
    }

    public BlockConnectionStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        Short mapping = reverseBlockMappings.get((short) blockState);
        if (mapping == null) return;
        blockState = mapping;
        long pair = getChunkSectionIndex(position);
        Pair<byte[], NibbleArray> map = getChunkSection(pair, (blockState & 0xF) != 0);
        int blockIndex = encodeBlockPos(position);
        map.getKey()[blockIndex] = (byte) (blockState >> 4);
        NibbleArray nibbleArray = map.getValue();
        if (nibbleArray != null) nibbleArray.set(blockIndex, blockState);
    }

    public int get(Position position) {
        long pair = getChunkSectionIndex(position);
        Pair<byte[], NibbleArray> map = blockStorage.get(pair);
        if (map == null) return 0;
        short blockPosition = encodeBlockPos(position);
        NibbleArray nibbleArray = map.getValue();
        return WorldPackets.toNewId(
                ((map.getKey()[blockPosition] & 0xFF) << 4)
                | (nibbleArray == null ? 0 : nibbleArray.get(blockPosition))
        );
    }

    public void remove(Position position) {
        long pair = getChunkSectionIndex(position);
        Pair<byte[], NibbleArray> map = blockStorage.get(pair);
        if (map == null) return;
        int blockIndex = encodeBlockPos(position);
        NibbleArray nibbleArray = map.getValue();
        if (nibbleArray != null) {
            nibbleArray.set(blockIndex, 0);
            boolean allZero = true;
            for (int i = 0; i < 4096; i++) {
                if (nibbleArray.get(i) != 0) {
                    allZero = false;
                    break;
                }
            }
            if (allZero) map.setValue(null);
        }
        map.getKey()[blockIndex] = 0;
        for (short entry : map.getKey()) {
            if (entry != 0) return;
        }
        blockStorage.remove(pair);
    }

    public void clear() {
        blockStorage.clear();
    }

    public void unloadChunk(int x, int z) {
        for (int y = 0; y < 256; y += 16) {
            blockStorage.remove(getChunkSectionIndex(x, y, z));
        }
    }

    private Pair<byte[], NibbleArray> getChunkSection(long index, boolean requireNibbleArray) {
        Pair<byte[], NibbleArray> map = blockStorage.get(index);
        if (map == null) {
            map = new Pair<>(new byte[4096], null);
            blockStorage.put(index, map);
        }
        if (map.getValue() == null && requireNibbleArray) {
            map.setValue(new NibbleArray(4096));
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
