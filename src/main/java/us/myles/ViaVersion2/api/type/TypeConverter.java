package us.myles.ViaVersion2.api.type;

public interface TypeConverter<T> {
    public T from(Object o);
}
