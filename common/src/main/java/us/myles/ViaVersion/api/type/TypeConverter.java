package us.myles.ViaVersion.api.type;

public interface TypeConverter<T> {
    /**
     * Convert from a type to the current type
     *
     * @param o The input object
     * @return The converted type as an object
     */
    T from(Object o);
}
