package us.myles.ViaVersion.compatibility.jre16;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Objects;
import us.myles.ViaVersion.compatibility.IFieldModifierAccessor;

@SuppressWarnings({
    "java:S1191", // SonarLint/-Qube/-Cloud: We (sadly) need Unsafe for the Java 16 impl.
    "java:S3011", // ^: We need to circumvent the access restrictions of fields.
})
public final class Jre16FieldModifierAccessor implements IFieldModifierAccessor {
  private final VarHandle modifiersHandle;

  public Jre16FieldModifierAccessor() throws ReflectiveOperationException {
    final Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    theUnsafeField.setAccessible(true);
    final sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);

    final Field trustedLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    final MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(
        unsafe.staticFieldBase(trustedLookup), unsafe.staticFieldOffset(trustedLookup));

    this.modifiersHandle = lookup.findVarHandle(Field.class, "modifiers", int.class);
  }

  @Override
  public void setModifiers(final Field field, final int modifiers) {
    Objects.requireNonNull(field, "field must not be null");

    this.modifiersHandle.set(field, modifiers);
  }
}
