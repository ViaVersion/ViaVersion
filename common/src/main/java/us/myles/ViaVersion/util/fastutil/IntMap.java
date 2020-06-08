package us.myles.ViaVersion.util.fastutil;

/**
 * Very simple wrapping interface to either be implemented by a HashMap or FastUtil's OpenHashMap.
 */
public interface IntMap {

    /**
     * @return value if present, -1 otherwise
     * @see java.util.HashMap#get(Object)
     */
    default int get(int key) {
        return getOrDefault(key, -1);
    }

    /**
     * @see java.util.HashMap#getOrDefault(Object, Object)
     */
    int getOrDefault(int key, int def);

    /**
     * @see java.util.HashMap#containsKey(Object)
     */
    boolean containsKey(int key);

    /**
     * @see java.util.HashMap#put(Object, Object)
     */
    int put(int key, int value);

    /**
     * @see java.util.HashMap#remove(Object)
     */
    int remove(int key);
}
