package us.myles.ViaVersion.util.filter;

/**
 * Created by Florian on 08.04.16 in us.myles.ViaVersion.util.filter
 */
public abstract class ResultIterator<T> {
    public abstract void iterate(T obj) throws Exception;
}
