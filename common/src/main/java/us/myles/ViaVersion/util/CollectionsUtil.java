package us.myles.ViaVersion.util;

import lombok.experimental.UtilityClass;
import us.myles.ViaVersion.api.Via;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class CollectionsUtil {
    private static Constructor<?> fastUtilLongObjectHashMap;
    private static Constructor<?> fastUtilIntObjectHashMap;
    private static Constructor<?> fastUtilIntHashSet;

    static {
        try {
            fastUtilLongObjectHashMap = getConstructor("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
            fastUtilIntObjectHashMap = getConstructor("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
            fastUtilIntHashSet = getConstructor("it.unimi.dsi.fastutil.ints.IntOpenHashSet", int.class);
            Via.getPlatform().getLogger().info("Using FastUtil for collections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    private static Constructor<?> getConstructor(String className, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        try {
            return Class.forName(className).getConstructor(parameterTypes);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        return Class.forName("org.bukkit.craftbukkit.libs." + className).getConstructor(parameterTypes);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<Long, T> createLongObjectMap() {
        if (fastUtilLongObjectHashMap != null) {
            try {
                return (Map<Long, T>) fastUtilLongObjectHashMap.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<Integer, T> createIntObjectMap() {
        if (fastUtilIntObjectHashMap != null) {
            try {
                return (Map<Integer, T>) fastUtilIntObjectHashMap.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static Set<Integer> createIntSet(int expected) {
        if (fastUtilIntHashSet != null) {
            try {
                return (Set<Integer>) fastUtilIntHashSet.newInstance(expected);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new HashSet<>(expected);
    }
}
