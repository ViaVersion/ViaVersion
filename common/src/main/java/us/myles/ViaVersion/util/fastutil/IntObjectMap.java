package us.myles.ViaVersion.util.fastutil;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Very simple wrapping interface to either be implemented by a HashMap or FastUtil's OpenHashMap.
 *
 * @param <V> Object value type
 */
public interface IntObjectMap<V> {

    /**
     * @see java.util.HashMap#get(Object)
     */
    @Nullable
    V get(int key);

    /**
     * @see java.util.HashMap#getOrDefault(Object, Object)
     */
    @Nullable
    V getOrDefault(int key, V def);

    /**
     * @see java.util.HashMap#containsKey(Object)
     */
    boolean containsKey(int key);

    /**
     * @see java.util.HashMap#put(Object, Object)
     */
    @Nullable
    V put(int key, V value);

    /**
     * @see java.util.HashMap#remove(Object)
     */
    @Nullable
    V remove(int key);

    /**
     * Returns the underlying map for usage of not implemented methods of this class.
     *
     * @return original map
     * @deprecated will cause wrapping if it is a FastUtil collection
     */
    @Deprecated
    Map<Integer, V> getMap();
}
