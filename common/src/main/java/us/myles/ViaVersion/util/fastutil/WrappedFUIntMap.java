package us.myles.ViaVersion.util.fastutil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Map;

final class WrappedFUIntMap implements IntMap {
    private final Int2IntMap map;

    WrappedFUIntMap(Map<Integer, Integer> originalMap) {
        this.map = new Int2IntOpenHashMap(originalMap);
    }

    WrappedFUIntMap(int size) {
        this.map = new Int2IntOpenHashMap(size);
    }

    @Override
    public int getOrDefault(int key, int def) {
        return map.getOrDefault(key, def);
    }

    @Override
    public boolean containsKey(int key) {
        return map.containsKey(key);
    }

    @Override
    public int put(int key, int value) {
        return map.put(key, value);
    }

    @Override
    public int remove(int key) {
        return map.remove(key);
    }
}