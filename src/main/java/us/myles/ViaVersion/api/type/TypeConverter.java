package us.myles.ViaVersion.api.type;

public interface TypeConverter<T> {
    T from(Object o);
}
