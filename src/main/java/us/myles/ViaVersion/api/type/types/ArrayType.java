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

    /* Taken from http://stackoverflow.com/questions/4901128/obtaining-the-array-class-of-a-component-type */
    public static Class<?> getArrayClass(Class<?> componentType) {
        ClassLoader classLoader = componentType.getClassLoader();
        String name;
        if (componentType.isArray()) {
            // just add a leading "["
            name = "[" + componentType.getName();
        } else if (componentType == boolean.class) {
            name = "[Z";
        } else if (componentType == byte.class) {
            name = "[B";
        } else if (componentType == char.class) {
            name = "[C";
        } else if (componentType == double.class) {
            name = "[D";
        } else if (componentType == float.class) {
            name = "[F";
        } else if (componentType == int.class) {
            name = "[I";
        } else if (componentType == long.class) {
            name = "[J";
        } else if (componentType == short.class) {
            name = "[S";
        } else {
            // must be an object non-array class
            name = "[L" + componentType.getName() + ";";
        }
        try {
            return classLoader != null ? classLoader.loadClass(name) : Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null; // oh
        }
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
