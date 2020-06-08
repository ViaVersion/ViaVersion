package us.myles.ViaVersion.util.fastutil;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to possibly wrap FastUtil collections for faster access.
 * These should only be used for high access and low/no change collections, since resizing FastUtil collections is expensive.
 */
public class CollectionUtil {
    private static final boolean FAST_UTIL = checkForFastUtil();

    private static boolean checkForFastUtil() {
        try {
            Class.forName("it.unimi.dsi.fastutil.ints.Int2IntMap");
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
        Preconditions.checkNotNull(originalMap);
        return FAST_UTIL ? new WrappedFUIntMap(originalMap) : new WrappedIntMap(originalMap);
    }

    /**
     * Creates a new FastUtil collection if present, else simply wraps a normal HashMap.
     *
     * @param size expected size of the collection
     * @return wrapped int map
     */
    public static IntMap createIntMap(int size) {
        return FAST_UTIL ? new WrappedFUIntMap(size) : new WrappedIntMap(size);
    }

    public static IntMap createIntMap() {
        return FAST_UTIL ? new WrappedFUIntMap(16) : new WrappedIntMap(new HashMap<>());
    }

    /**
     * Creates a new FastUtil collection from the given map if present, else simply wraps the original.
     *
     * @param originalMap map to be reflected
     * @return wrapped int map
     */
    public static <V> IntObjectMap<V> createIntObjectMap(Map<Integer, V> originalMap) {
        Preconditions.checkNotNull(originalMap);
        return FAST_UTIL ? new WrappedFUIntObjectMap<>(originalMap) : new WrappedIntObjectMap<>(originalMap);
    }

    /**
     * Creates a new FastUtil collection if present, else simply wraps a normal HashMap.
     *
     * @param size expected size of the collection
     * @return wrapped int map
     */
    public static <V> IntObjectMap<V> createIntObjectMap(int size) {
        return FAST_UTIL ? new WrappedFUIntObjectMap<>(size) : new WrappedIntObjectMap<>(size);
    }

    /**
     * Creates a new FastUtil collection from the given set if present, else simply wraps the original.
     *
     * @param originalSet set to be reflected
     * @return wrapped int set
     */
    public static IntSet createIntSet(Set<Integer> originalSet) {
        Preconditions.checkNotNull(originalSet);
        return FAST_UTIL ? new WrappedFUIntSet(originalSet) : new WrappedIntSet(originalSet);
    }

    /**
     * Creates a new FastUtil collection if present, else simply wraps a normal HashSet.
     *
     * @param size expected size of the collection
     * @return wrapped int set
     */
    public static IntSet createIntSet(int size) {
        return FAST_UTIL ? new WrappedFUIntSet(size) : new WrappedIntSet(size);
    }

    public static boolean hasFastUtil() {
        return FAST_UTIL;
    }
}
