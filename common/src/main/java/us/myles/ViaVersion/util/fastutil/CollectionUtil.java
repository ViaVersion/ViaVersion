package us.myles.ViaVersion.util.fastutil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to possibly wrap FastUtil collections for faster access.
 * These should only be used for high access and low/no change collections, since resizing FastUtil collections is expensive.
 */
public class CollectionUtil {
    private static final boolean FAST_UTIL = hasFastUtil();

    private static boolean hasFastUtil() {
        try {
            Class.forName("Int2IntMap");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a new FastUtil collection from the given map if present, else simply wraps the original.
     *
     * @param originalMap map to be reflected
     * @return wrapped int map
     */
    public static IntMap createIntMap(Map<Integer, Integer> originalMap) {
        return FAST_UTIL ? new WrappedFUIntMap(new Int2IntOpenHashMap(originalMap)) : new WrappedIntMap(originalMap);
    }

    /**
     * Creates a new FastUtil collection if present, else simply wraps a normal HashMap.
     *
     * @param size expected size of the collection
     * @return wrapped int map
     */
    public static IntMap createIntMap(int size) {
        return FAST_UTIL ? new WrappedFUIntMap(new Int2IntOpenHashMap(size)) : new WrappedIntMap(new HashMap<>(size));
    }

    public static IntMap createIntMap() {
        return FAST_UTIL ? new WrappedFUIntMap(new Int2IntOpenHashMap()) : new WrappedIntMap(new HashMap<>());
    }

    /**
     * Creates a new FastUtil collection from the given set if present, else simply wraps the original.
     *
     * @param originalSet set to be reflected
     * @return wrapped int set
     */
    public static IntSet createIntSet(Set<Integer> originalSet) {
        return FAST_UTIL ? new WrappedFUIntSet(new IntOpenHashSet(originalSet)) : new WrappedIntSet(originalSet);
    }

    /**
     * Creates a new FastUtil collection if present, else simply wraps a normal HashSet.
     *
     * @param size expected size of the collection
     * @return wrapped int set
     */
    public static IntSet createIntSet(int size) {
        return FAST_UTIL ? new WrappedFUIntSet(new IntOpenHashSet(size)) : new WrappedIntSet(new HashSet<>(size));
    }

    private static final class WrappedFUIntMap implements IntMap {
        private final Int2IntMap map;

        private WrappedFUIntMap(Int2IntMap map) {
            this.map = map;
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

    private static final class WrappedIntMap implements IntMap {
        private final Map<Integer, Integer> map;

        private WrappedIntMap(Map<Integer, Integer> map) {
            this.map = map;
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

    private static final class WrappedFUIntSet implements IntSet {
        private final it.unimi.dsi.fastutil.ints.IntSet set;

        private WrappedFUIntSet(it.unimi.dsi.fastutil.ints.IntSet set) {
            this.set = set;
        }

        @Override
        public boolean contains(int key) {
            return set.contains(key);
        }

        @Override
        public boolean add(int key) {
            return set.add(key);
        }

        @Override
        public boolean remove(int key) {
            return set.remove(key);
        }
    }

    private static final class WrappedIntSet implements IntSet {
        private final Set<Integer> set;

        private WrappedIntSet(Set<Integer> set) {
            this.set = set;
        }

        @Override
        public boolean contains(int key) {
            return set.contains(key);
        }

        @Override
        public boolean add(int key) {
            return set.add(key);
        }

        @Override
        public boolean remove(int key) {
            return set.remove(key);
        }
    }
}
