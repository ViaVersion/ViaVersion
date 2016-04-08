package us.myles.ViaVersion.util.filter;

/**
 * Created by Florian on 08.04.16 in us.myles.ViaVersion.util.filter
 */
public abstract class IFilter<T> {
    public abstract boolean filter(T object, Object in) throws Exception;

    public abstract String type();
}
