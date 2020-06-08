package us.myles.ViaVersion.util.fastutil;

/**
 * Very simple wrapping interface to either be implemented by a HashSet or FastUtil's OpenHashSet.
 */
public interface IntSet {

    /**
     * @see java.util.HashSet#contains(Object)
     */
    boolean contains(int key);

    /**
     * @see java.util.HashSet#add(Object)
     */
    boolean add(int key);

    /**
     * @see java.util.HashSet#remove(Object)
     */
    boolean remove(int key);
}
