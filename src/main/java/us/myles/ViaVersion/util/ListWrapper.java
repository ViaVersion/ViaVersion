package us.myles.ViaVersion.util;

import java.util.*;

public abstract class ListWrapper implements List {
    public final Object lock = new Object();
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
        synchronized (lock) {
            return this.list.size();
        }
    }

    @Override
    public synchronized boolean isEmpty() {
        synchronized (lock) {
            return this.list.isEmpty();
        }
    }

    @Override
    public synchronized boolean contains(Object o) {
        synchronized (lock) {
            return this.list.contains(o);
        }
    }

    @Override
    public synchronized Iterator iterator() {
        synchronized (lock) {
            return listIterator();
        }
    }

    @Override
    public synchronized Object[] toArray() {
        synchronized (lock) {
            return this.list.toArray();
        }
    }

    @Override
    public synchronized boolean add(Object o) {
        synchronized (lock) {
            handleAdd(o);
            return this.list.add(o);
        }
    }

    @Override
    public synchronized boolean remove(Object o) {
        synchronized (lock) {
            return this.list.remove(o);
        }
    }

    @Override
    public synchronized boolean addAll(Collection c) {
        synchronized (lock) {
            for (Object o : c) {
                handleAdd(o);
            }
            return this.list.addAll(c);
        }
    }

    @Override
    public synchronized boolean addAll(int index, Collection c) {
        synchronized (lock) {
            for (Object o : c) {
                handleAdd(o);
            }
            return this.list.addAll(index, c);
        }
    }

    @Override
    public synchronized void clear() {
        synchronized (lock) {
            this.list.clear();
        }
    }

    @Override
    public synchronized Object get(int index) {
        synchronized (lock) {
            return this.list.get(index);
        }
    }

    @Override
    public synchronized Object set(int index, Object element) {
        synchronized (lock) {
            return this.list.set(index, element);
        }
    }

    @Override
    public synchronized void add(int index, Object element) {
        synchronized (lock) {
            this.list.add(index, element);
        }
    }

    @Override
    public synchronized Object remove(int index) {
        synchronized (lock) {
            return this.list.remove(index);
        }
    }

    @Override
    public synchronized int indexOf(Object o) {
        synchronized (lock) {
            return this.list.indexOf(o);
        }
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        synchronized (lock) {
            return this.list.lastIndexOf(o);
        }
    }

    @Override
    public synchronized ListIterator listIterator() {
        synchronized (lock) {
            return this.list.listIterator();
        }
    }

    @Override
    public synchronized ListIterator listIterator(int index) {
        synchronized (lock) {
            return this.list.listIterator(index);
        }
    }

    @Override
    public synchronized List subList(int fromIndex, int toIndex) {
        synchronized (lock) {
            return this.list.subList(fromIndex, toIndex);
        }
    }

    @Override
    public synchronized boolean retainAll(Collection c) {
        synchronized (lock) {
            return this.list.retainAll(c);
        }
    }

    @Override
    public synchronized boolean removeAll(Collection c) {
        synchronized (lock) {
            return this.list.removeAll(c);
        }
    }

    @Override
    public synchronized boolean containsAll(Collection c) {
        synchronized (lock) {
            return this.list.containsAll(c);
        }
    }

    @Override
    public synchronized Object[] toArray(Object[] a) {
        synchronized (lock) {
            return this.list.toArray(a);
        }
    }
}
