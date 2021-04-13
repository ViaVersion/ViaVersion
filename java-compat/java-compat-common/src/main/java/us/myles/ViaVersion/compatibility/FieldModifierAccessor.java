package us.myles.ViaVersion.compatibility;

import java.lang.reflect.Field;

/**
 * Exposes a way to access the modifiers of a {@link Field} mutably.
 * <p>
 * <i>Note:</i> This is <b>explicitly</b> an implementation detail. Do not rely on this within plugins and any
 * non-ViaVersion code.
 * </p>
 */
public interface FieldModifierAccessor {
    /**
     * Sets the modifiers of a field.
     * <p>
     * <i>Note:</i> This does not set the accessibility of the field. If you need to read or mutate it, you must handle
     * that yourself.
     * </p>
     *
     * @param field     the field to set the modifiers of. Will throw if {@code null}.
     * @param modifiers the modifiers to set on the given {@code field}.
     * @throws ReflectiveOperationException if the reflective operation fails this method is implemented with fails.
     */
    void setModifiers(final Field field, final int modifiers)
            throws ReflectiveOperationException;
}
