package us.myles.ViaVersion.util;

import lombok.experimental.UtilityClass;
import us.myles.ViaVersion.api.Via;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class CollectionsUtil {
    private static Constructor<?> fastUtilLongObjectHashMap;
    private static Constructor<?> fastUtilIntObjectHashMap;

    static {
        try {
            fastUtilLongObjectHashMap = ReflectionUtil.getClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").getConstructor();
            fastUtilIntObjectHashMap = ReflectionUtil.getClass("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap").getConstructor();
            Via.getPlatform().getLogger().info("Using FastUtil for collections");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
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
}
