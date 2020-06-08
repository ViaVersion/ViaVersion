package us.myles.ViaVersion.util.fastutil;

import java.util.HashMap;
import java.util.Map;

final class WrappedIntMap implements IntMap {
    private final Map<Integer, Integer> map;

    WrappedIntMap(Map<Integer, Integer> originalMap) {
        this.map = originalMap;
    }

    WrappedIntMap(int size) {
        this.map = new HashMap<>(size);
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
        Integer oldValue = map.put(key, value);
        return oldValue != null ? oldValue : -1;
    }

    @Override
    public int remove(int key) {
        return map.remove(key);
    }
}
