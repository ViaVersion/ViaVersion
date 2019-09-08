package us.myles.ViaVersion.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class InvokeUtil {

    private static final MethodHandles.Lookup LOOKUP;

    private InvokeUtil() { }

    public static MethodHandles.Lookup lookup() {
        return LOOKUP;
    }

    static {
        try {
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            LOOKUP = (MethodHandles.Lookup) field.get(null);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }
}
