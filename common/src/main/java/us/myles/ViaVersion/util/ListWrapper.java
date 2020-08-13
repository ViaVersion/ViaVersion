package us.myles.ViaVersion.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @deprecated scary
 */
@Deprecated
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
    public boolean contains(Object o) {
        synchronized (this) {
            return this.list.contains(o);
        }
    }

    @Override
    public Iterator iterator() {
        synchronized (this) {
            return listIterator();
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (this) {
            return this.list.toArray();
        }
    }

    @Override
    public boolean add(Object o) {
        handleAdd(o);
        synchronized (this) {
            return this.list.add(o);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            return this.list.remove(o);
        }
    }

    @Override
    public boolean addAll(Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        synchronized (this) {
            return this.list.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection c) {
        for (Object o : c) {
            handleAdd(o);
        }
        synchronized (this) {
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
    public Object get(int index) {
        synchronized (this) {
            return this.list.get(index);
        }
    }

    @Override
    public Object set(int index, Object element) {
        synchronized (this) {
            return this.list.set(index, element);
        }
    }

    @Override
    public void add(int index, Object element) {
        synchronized (this) {
            this.list.add(index, element);
        }
    }

    @Override
    public Object remove(int index) {
        synchronized (this) {
            return this.list.remove(index);
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this) {
            return this.list.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this) {
            return this.list.lastIndexOf(o);
        }
    }

    @Override
    public ListIterator listIterator() {
        synchronized (this) {
            return this.list.listIterator();
        }
    }

    @Override
    public ListIterator listIterator(int index) {
        synchronized (this) {
            return this.list.listIterator(index);
        }
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        synchronized (this) {
            return this.list.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean retainAll(Collection c) {
        synchronized (this) {
            return this.list.retainAll(c);
        }
    }

    @Override
    public boolean removeAll(Collection c) {
        synchronized (this) {
            return this.list.removeAll(c);
        }
    }

    @Override
    public boolean containsAll(Collection c) {
        synchronized (this) {
            return this.list.containsAll(c);
        }
    }

    @Override
    public Object[] toArray(Object[] a) {
        synchronized (this) {
            return this.list.toArray(a);
        }
    }
}
