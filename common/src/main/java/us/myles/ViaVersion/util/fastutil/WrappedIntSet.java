package us.myles.ViaVersion.util.fastutil;

import java.util.HashSet;
import java.util.Set;

final class WrappedIntSet implements IntSet {
    private final Set<Integer> set;

    WrappedIntSet(Set<Integer> originalSet) {
        this.set = originalSet;
    }

    WrappedIntSet(int size) {
        this.set = new HashSet<>(size);
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
