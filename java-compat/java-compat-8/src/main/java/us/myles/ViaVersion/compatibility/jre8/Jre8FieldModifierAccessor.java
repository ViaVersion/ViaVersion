package us.myles.ViaVersion.compatibility.jre8;

import java.lang.reflect.Field;
import java.util.Objects;
import us.myles.ViaVersion.compatibility.IFieldModifierAccessor;

@SuppressWarnings("java:S3011") // SonarLint/-Qube/-Cloud: we are intentionally bypassing the setter.
public final class Jre8FieldModifierAccessor implements IFieldModifierAccessor {
  private final Field modifiersField;

  public Jre8FieldModifierAccessor() throws ReflectiveOperationException {
    this.modifiersField = Field.class.getDeclaredField("modifiers");
    this.modifiersField.setAccessible(true);
  }

  @Override
  public void setModifiers(final Field field, final int modifiers) throws ReflectiveOperationException {
    Objects.requireNonNull(field, "field must not be null");

    this.modifiersField.setInt(field, modifiers);
  }
}
