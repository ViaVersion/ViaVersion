package us.myles.ViaVersion.util.fastutil;

import java.util.HashMap;
import java.util.Map;

final class WrappedIntObjectMap<V> implements IntObjectMap<V> {
    private final Map<Integer, V> map;

    WrappedIntObjectMap(Map<Integer, V> originalMap) {
        this.map = originalMap;
    }

    WrappedIntObjectMap(int size) {
        this.map = new HashMap<>(size);
    }

    @Override
    public V get(int key) {
        return map.get(key);
    }

    @Override
    public V getOrDefault(int key, V def) {
        return map.getOrDefault(key, def);
    }

    @Override
    public boolean containsKey(int key) {
        return map.containsKey(key);
    }

    @Override
    public V put(int key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(int key) {
        return map.remove(key);
    }

    @Override
    public Map<Integer, V> getMap() {
        return map;
    }
}