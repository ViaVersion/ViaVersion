package us.myles.ViaVersion.util;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtocolSupportUtil {
    private static Method protocolVersionMethod = null;
    private static Method getIdMethod = null;

    static {
        try {
            protocolVersionMethod = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class);
            getIdMethod = Class.forName("protocolsupport.api.ProtocolVersion").getMethod("getId");
        } catch (Exception e) {
            // ProtocolSupport not installed.
        }
    }

    public static int getProtocolVersion(Player player) {
        if (protocolVersionMethod == null) return -1;
        try {
            Object version = protocolVersionMethod.invoke(null, player);
            return (int) getIdMethod.invoke(version);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
