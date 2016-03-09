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

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override
    public Iterator iterator() {
        return this.list.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public boolean add(Object o) {
        handleAdd(o);
        return this.list.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    @Override
    public boolean addAll(Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        return this.list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        return this.list.addAll(index, c);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public Object get(int index) {
        return this.list.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return this.list.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        this.list.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return this.list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        return this.list.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return this.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(Collection c) {
        return this.list.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return this.list.removeAll(c);
    }

    @Override
    public boolean containsAll(Collection c) {
        return this.list.containsAll(c);
    }

    @Override
    public Object[] toArray(Object[] a) {
        return this.list.toArray(a);
    }
}
