package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

import java.lang.reflect.Array;

public class ArrayType<T> extends Type<T[]> {
    private final Type<T> elementType;

    public ArrayType(Type<T> type) {
        super(type.getTypeName() + " Array", (Class<T[]>) getArrayClass(type.getOutputClass()));
        this.elementType = type;
    }

    public static Class<?> getArrayClass(Class<?> componentType) {
        // Should only happen once per class init.
        return Array.newInstance(componentType, 0).getClass();
    }

    @Override
    public T[] read(ByteBuf buffer) throws Exception {
        int amount = Type.VAR_INT.read(buffer);
        T[] array = (T[]) Array.newInstance(elementType.getOutputClass(), amount);

        for (int i = 0; i < amount; i++) {
            array[i] = elementType.read(buffer);
        }
        return array;
    }

    @Override
    public void write(ByteBuf buffer, T[] object) throws Exception {
        Type.VAR_INT.write(buffer, object.length);
        for (T o : object) {
            elementType.write(buffer, o);
        }
    }
}
