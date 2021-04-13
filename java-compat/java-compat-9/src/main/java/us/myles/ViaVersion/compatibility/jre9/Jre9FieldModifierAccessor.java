package us.myles.ViaVersion.compatibility.jre9;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Objects;
import us.myles.ViaVersion.compatibility.FieldModifierAccessor;

public final class Jre9FieldModifierAccessor implements FieldModifierAccessor {
  private final VarHandle modifiersHandle;

  public Jre9FieldModifierAccessor() throws ReflectiveOperationException {
    final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
    this.modifiersHandle = lookup.findVarHandle(Field.class, "modifiers", int.class);
  }

  @Override
  public void setModifiers(final Field field, final int modifiers) {
    Objects.requireNonNull(field, "field must not be null");

    this.modifiersHandle.set(field, modifiers);
  }
}
