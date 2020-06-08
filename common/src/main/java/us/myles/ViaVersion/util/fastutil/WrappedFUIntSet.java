package us.myles.ViaVersion.util.fastutil;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.Set;

final class WrappedFUIntSet implements IntSet {
    private final it.unimi.dsi.fastutil.ints.IntSet set;

    WrappedFUIntSet(Set<Integer> originalSet) {
        this.set = new IntOpenHashSet(originalSet);
    }

    WrappedFUIntSet(int size) {
        this.set = new IntOpenHashSet(size);
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