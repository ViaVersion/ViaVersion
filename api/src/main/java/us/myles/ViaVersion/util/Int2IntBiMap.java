package us.myles.ViaVersion.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * Simple wrapper class for two {@link Int2IntMap}s.
 *
 * @see #inverse() to get the inversed map
 */
public class Int2IntBiMap implements Int2IntMap {

    private final Int2IntMap map;
    private final Int2IntBiMap inverse;

    public Int2IntBiMap() {
        this.map = new Int2IntOpenHashMap();
        this.inverse = new Int2IntBiMap(this);
    }

    private Int2IntBiMap(Int2IntBiMap inverse) {
        this.map = new Int2IntOpenHashMap();
        this.inverse = inverse;
    }

    /**
     * @return the inverse of this bimap
     */
    public Int2IntBiMap inverse() {
        return inverse;
    }

    /**
     * Puts the key and value into the maps.
     *
     * @param key   key
     * @param value value
     * @return old value if present
     * @throws IllegalArgumentException if the value already exists in the map
     */
    @Override
    public int put(int key, int value) {
        if (containsKey(key) && value == get(key)) return value;

        Preconditions.checkArgument(!containsValue(value), "value already present: %s", value);
        map.put(key, value);
        inverse.map.put(value, key);
        return defaultReturnValue();
    }

    @Override
    public boolean remove(int key, int value) {
        map.remove(key, value);
        return inverse.map.remove(key, value);
    }

    @Override
    public int get(int key) {
        return map.get(key);
    }

    @Override
    public void clear() {
        map.clear();
        inverse.map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @Deprecated
    public void putAll(@NotNull Map<? extends Integer, ? extends Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(int rv) {
        map.defaultReturnValue(rv);
        inverse.map.defaultReturnValue(rv);
    }

    @Override
    public int defaultReturnValue() {
        return map.defaultReturnValue();
    }

    @Override
    public ObjectSet<Entry> int2IntEntrySet() {
        return map.int2IntEntrySet();
    }

    @Override
    @NotNull
    public IntSet keySet() {
        return map.keySet();
    }

    @Override
    @NotNull
    public IntSet values() {
        return inverse.map.keySet();
    }

    @Override
    public boolean containsKey(int key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(int value) {
        return inverse.map.containsKey(value);
    }
}
