package us.myles.ViaVersion.api;

import lombok.Getter;
import us.myles.ViaVersion.ViaVersionPlugin;

public class ViaVersion {

    @Getter
    private static ViaVersionAPI instance;
    @Getter
    private static ViaVersionConfig config;

    public static void setInstance(ViaVersionPlugin plugin) {
        ViaVersion.instance = plugin;
        ViaVersion.config = plugin;
    }
}
