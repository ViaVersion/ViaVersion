package us.myles.ViaVersion.util.fastutil;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

final class WrappedFUIntObjectMap<V> implements IntObjectMap<V> {
    private final Int2ObjectMap<V> map;

    WrappedFUIntObjectMap(Map<Integer, V> originalMap) {
        this.map = new Int2ObjectOpenHashMap<>(originalMap);
    }

    WrappedFUIntObjectMap(int size) {
        this.map = new Int2ObjectOpenHashMap(size);
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