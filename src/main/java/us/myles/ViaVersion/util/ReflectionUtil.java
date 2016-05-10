package us.myles.ViaVersion.util;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ReflectionUtil {
    private static String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static String NMS = BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> nms(String className) throws ClassNotFoundException {
        return Class.forName(NMS + "." + className);
    }

    public static Class<?> obc(String className) throws ClassNotFoundException {
        return Class.forName(BASE + "." + className);
    }

    public static String getVersion() {
        return BASE.substring(BASE.lastIndexOf('.') + 1);
    }

    public static Object invokeStatic(Class<?> clazz, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = clazz.getDeclaredMethod(method);
        return m.invoke(null);
    }

    public static Object invoke(Object o, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = o.getClass().getDeclaredMethod(method);
        return m.invoke(o);
    }

    public static <T> T getStatic(Class<?> clazz, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(null);
    }

    public static <T> T getSuper(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getSuperclass().getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    public static <T> T get(Object instance, Class<?> clazz, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(instance);
    }

    public static <T> T get(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    public static <T> T getPublic(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }


    public static void set(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(o, value);
    }

    public static final class ClassReflection {
        private final Class<?> handle;
        private final Map<String, Field> fields = Maps.newConcurrentMap();
        private final Map<String, Method> methods = Maps.newConcurrentMap();

        public ClassReflection(Class<?> handle) {
            this(handle, true);
        }

        public ClassReflection(Class<?> handle, boolean recursive) {
            this.handle = handle;
            scanFields(handle, recursive);
            scanMethods(handle, recursive);
        }

        private void scanFields(Class<?> host, boolean recursive) {
            if (host.getSuperclass() != null && recursive) {
                scanFields(host.getSuperclass(), true);
            }

            for (Field field : host.getDeclaredFields()) {
                field.setAccessible(true);
                fields.put(field.getName(), field);
            }
        }

        private void scanMethods(Class<?> host, boolean recursive) {
            if (host.getSuperclass() != null && recursive) {
                scanMethods(host.getSuperclass(), true);
            }

            for (Method method : host.getDeclaredMethods()) {
                method.setAccessible(true);
                methods.put(method.getName(), method);
            }
        }

        public Object newInstance() throws IllegalAccessException, InstantiationException {
            return handle.newInstance();
        }

        public Field getField(String name) {
            return fields.get(name);
        }

        public void setFieldValue(String fieldName, Object instance, Object value) throws IllegalAccessException {
            getField(fieldName).set(instance, value);
        }

        public <T> T getFieldValue(String fieldName, Object instance, Class<T> type) throws IllegalAccessException {
            return type.cast(getField(fieldName).get(instance));
        }

        public <T> T invokeMethod(Class<T> type, String methodName, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
            return type.cast(getMethod(methodName).invoke(instance, args));
        }

        public Method getMethod(String name) {
            return methods.get(name);
        }

        public Collection<Field> getFields() {
            return Collections.unmodifiableCollection(fields.values());
        }

        public Collection<Method> getMethods() {
            return Collections.unmodifiableCollection(methods.values());
        }
    }
}
