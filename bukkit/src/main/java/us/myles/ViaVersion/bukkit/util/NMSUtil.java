package us.myles.ViaVersion.bukkit.util;

import org.bukkit.Bukkit;

public class NMSUtil {
    private static final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS = BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    private static final boolean DEBUG_PROPERTY = loadDebugProperty();

    private static boolean loadDebugProperty() {
        try {
            Class<?> serverClass = nms("MinecraftServer");
            Object server = serverClass.getDeclaredMethod("getServer").invoke(null);
            return (boolean) serverClass.getMethod("isDebugging").invoke(server);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static Class<?> nms(String className) throws ClassNotFoundException {
        return Class.forName(NMS + "." + className);
    }

    public static Class<?> obc(String className) throws ClassNotFoundException {
        return Class.forName(BASE + "." + className);
    }

    public static String getVersion() {
        return BASE.substring(BASE.lastIndexOf('.') + 1);
    }

    /**
     * @return true if debug=true is set in the server.properties (added by CB)
     */
    public static boolean isDebugPropertySet() {
        return DEBUG_PROPERTY;
    }
}
