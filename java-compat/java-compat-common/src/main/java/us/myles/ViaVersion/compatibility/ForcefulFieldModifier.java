package us.myles.ViaVersion.compatibility;

import java.lang.reflect.Field;

/**
 * Exposes a way to modify a {@link Field}, regardless of its limitations (given it is accessible by the caller).
 * <p>
 * <i>Note:</i> This is <b>explicitly</b> an implementation detail. Do not rely on this within plugins and any
 * non-ViaVersion code.
 * </p>
 */
public interface ForcefulFieldModifier {
    /**
     * Sets the field regardless of field finality.
     * <p>
     * <i>Note:</i> This does not set the accessibility of the field.
     * </p>
     *
     * @param field the field to set the modifiers of. Will throw if {@code null}.
     * @param holder the eye of the beholder. For static fields, use {@code null}.
     * @param object the new value to set of the object.
     */
    void setField(final Field field, final Object holder, final Object object)
        throws ReflectiveOperationException;
}
