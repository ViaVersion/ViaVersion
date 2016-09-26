package us.myles.ViaVersion.util;

import org.bukkit.Bukkit;

public class NMSUtil {
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
}
