package us.myles.ViaVersion.compatibility.unsafe;

import us.myles.ViaVersion.compatibility.ForcefulFieldModifier;

import java.lang.reflect.Field;
import java.util.Objects;

@SuppressWarnings({
        "java:S1191", // SonarLint/-Qube/-Cloud: We need Unsafe for the modifier implementation.
        "java:S3011", // ^: We need to circumvent the access restrictions of fields.
})
public final class UnsafeBackedForcefulFieldModifier implements ForcefulFieldModifier {
    private final sun.misc.Unsafe unsafe;

    public UnsafeBackedForcefulFieldModifier() throws ReflectiveOperationException {
        final Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        this.unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
    }

    @Override
    public void setField(final Field field, final Object holder, final Object object) {
        Objects.requireNonNull(field, "field must not be null");

        final Object ufo = holder != null ? holder : this.unsafe.staticFieldBase(field);
        final long offset = holder != null ? this.unsafe.objectFieldOffset(field) : this.unsafe.staticFieldOffset(field);

        this.unsafe.putObject(ufo, offset, object);
    }
}
