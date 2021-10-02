package com.viaversion.viaversion.util;

import com.google.common.collect.ForwardingSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class SetWrapper<E> extends ForwardingSet<E> {

    private final Set<E> set;
    private final Consumer<E> addListener;

    public SetWrapper(Set<E> set, Consumer<E> addListener) {
        this.set = set;
        this.addListener = addListener;
    }

    @Override
    public boolean add(@NonNull E element) {
        addListener.accept(element);

        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            addListener.accept(element);
        }

        return super.addAll(collection);
    }

    @Override
    protected Set<E> delegate() {
        return originalSet();
    }

    public Set<E> originalSet() {
        return this.set;
    }
}
