package us.myles.ViaVersion.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class ListWrapper implements List {
    private final List list;

    public ListWrapper(List inputList) {
        this.list = inputList;
    }

    public abstract void handleAdd(Object o);

    public List getOriginalList() {
        return list;
    }

    @Override
    public synchronized int size() {
        return this.list.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override
    public synchronized Iterator iterator() {
        return this.list.iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public synchronized boolean add(Object o) {
        handleAdd(o);
        return this.list.add(o);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return this.list.remove(o);
    }

    @Override
    public synchronized boolean addAll(Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        return this.list.addAll(c);
    }

    @Override
    public synchronized boolean addAll(int index, Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        return this.list.addAll(index, c);
    }

    @Override
    public synchronized void clear() {
        this.list.clear();
    }

    @Override
    public synchronized Object get(int index) {
        return this.list.get(index);
    }

    @Override
    public synchronized Object set(int index, Object element) {
        return this.list.set(index, element);
    }

    @Override
    public synchronized void add(int index, Object element) {
        this.list.add(index, element);
    }

    @Override
    public synchronized Object remove(int index) {
        return this.list.remove(index);
    }

    @Override
    public synchronized int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override
    public synchronized ListIterator listIterator() {
        return this.list.listIterator();
    }

    @Override
    public synchronized ListIterator listIterator(int index) {
        return this.list.listIterator(index);
    }

    @Override
    public synchronized List subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }

    @Override
    public synchronized boolean retainAll(Collection c) {
        return this.list.retainAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection c) {
        return this.list.removeAll(c);
    }

    @Override
    public synchronized boolean containsAll(Collection c) {
        return this.list.containsAll(c);
    }

    @Override
    public synchronized Object[] toArray(Object[] a) {
        return this.list.toArray(a);
    }
}
