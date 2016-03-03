package us.myles.ViaVersion.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private static String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static String NMS = BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> nms(String className) throws ClassNotFoundException {
        return Class.forName(NMS + "." + className);
    }
    public static Class<?> obc(String className) throws ClassNotFoundException {
        return Class.forName(BASE + "." + className);
    }

    public static Object invokeStatic(Class<?> clazz, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = clazz.getDeclaredMethod(method);
        return m.invoke(null);
    }

    public static Object invoke(Object o, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = o.getClass().getDeclaredMethod(method);
        return m.invoke(o);
    }

    public static <T> T get(Object o, String f, Class<T> t) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    public static void set(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(o, value);
    }
}
