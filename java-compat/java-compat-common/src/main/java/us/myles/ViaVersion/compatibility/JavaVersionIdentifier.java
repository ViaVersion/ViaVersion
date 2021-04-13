package us.myles.ViaVersion.compatibility;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

public enum JavaVersionIdentifier {
    ;

    public static final boolean IS_JAVA_9;
    public static final boolean IS_JAVA_16;

    static {
        // Optional<T>#stream()Stream<T> is marked `@since 9`.
        IS_JAVA_9 = doesMethodExist(Optional.class, "stream");

        // Stream<T>#toList()List<T> is marked `@since 16`.
        IS_JAVA_16 = doesMethodExist(Stream.class, "toList");
    }

    /**
     * Checks if the given name of a {@link Method} exists on the given {@link Class} without comparing parameters or
     * other parts of the descriptor. The method must be public and declared on the given class.
     * <p>
     * <i>Note:</i> This should only check for stable methods that are expected to stay permanently.
     * </p>
     *
     * @param clazz  the type to get the given {@code method} on.
     * @param method the name to find.
     * @return whether the given method exists.
     */
    private static boolean doesMethodExist(final Class<?> clazz, final String method) {
        for (final Method reflect : clazz.getMethods()) {
            if (reflect.getName().equals(method)) {
                return true;
            }
        }

        return false;
    }
}
