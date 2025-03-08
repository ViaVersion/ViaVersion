/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Synchronized list wrapper with the addition of an add handler called when an element is added to the list.
 *
 * @param <E> list type
 */
public final class SynchronizedListWrapper<E> implements List<E> {
    private final List<E> list;
    private final Consumer<E> addHandler;

    public SynchronizedListWrapper(final List<E> inputList, final Consumer<E> addHandler) {
        this.list = inputList;
        this.addHandler = addHandler;
    }

    public List<E> originalList() {
        return list;
    }

    private void handleAdd(E o) {
        addHandler.accept(o);
    }

    @Override
    public int size() {
        synchronized (this) {
            return this.list.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return this.list.isEmpty();
        }
    }

    @Override
    public boolean contains(final Object o) {
        synchronized (this) {
            return this.list.contains(o);
        }
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        // Has to be manually synced
        return listIterator();
    }

    @Override
    public Object @NonNull [] toArray() {
        synchronized (this) {
            return this.list.toArray();
        }
    }

    @Override
    public boolean add(final E o) {
        synchronized (this) {
            handleAdd(o);
            return this.list.add(o);
        }
    }

    @Override
    public boolean remove(final Object o) {
        synchronized (this) {
            return this.list.remove(o);
        }
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        synchronized (this) {
            for (final E o : c) {
                handleAdd(o);
            }
            return this.list.addAll(c);
        }
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        synchronized (this) {
            for (final E o : c) {
                handleAdd(o);
            }
            return this.list.addAll(index, c);
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.list.clear();
        }
    }

    @Override
    public E get(final int index) {
        synchronized (this) {
            return this.list.get(index);
        }
    }

    @Override
    public E set(final int index, final E element) {
        synchronized (this) {
            return this.list.set(index, element);
        }
    }

    @Override
    public void add(final int index, final E element) {
        synchronized (this) {
            this.list.add(index, element);
        }
    }

    @Override
    public E remove(final int index) {
        synchronized (this) {
            return this.list.remove(index);
        }
    }

    @Override
    public int indexOf(final Object o) {
        synchronized (this) {
            return this.list.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(final Object o) {
        synchronized (this) {
            return this.list.lastIndexOf(o);
        }
    }

    @Override
    public @NonNull ListIterator<E> listIterator() {
        // Has to be manually synced
        return this.list.listIterator();
    }

    @Override
    public @NonNull ListIterator<E> listIterator(final int index) {
        // Has to be manually synced
        return this.list.listIterator(index);
    }

    @Override
    public @NonNull List<E> subList(final int fromIndex, final int toIndex) {
        // Not perfect
        synchronized (this) {
            return this.list.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {
        synchronized (this) {
            return this.list.retainAll(c);
        }
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        synchronized (this) {
            return this.list.removeAll(c);
        }
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> c) {
        synchronized (this) {
            return this.list.containsAll(c);
        }
    }

    @Override
    public <T> T @NonNull [] toArray(final T @NonNull [] a) {
        synchronized (this) {
            return this.list.toArray(a);
        }
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        synchronized (this) {
            list.sort(c);
        }
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        synchronized (this) {
            list.forEach(consumer);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        synchronized (this) {
            return list.removeIf(filter);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        synchronized (this) {
            return list.equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (this) {
            return list.hashCode();
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return list.toString();
        }
    }
}
