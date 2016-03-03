package ml.minelight.util;

import org.bukkit.Bukkit;

public class Utils {
	public static void logToConsole(boolean severe, String msg) {
		if (severe) {
			Bukkit.getLogger().severe("[ViaVersion] " + msg);
		} else {
			Bukkit.getLogger().info("[ViaVersion] " + msg);
		}
	}
}
