package us.myles.ViaVersion.api;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import us.myles.ViaVersion.ViaVersionPlugin;

public class ViaVersion {

    @Getter
    private static ViaVersionAPI instance;
    @Getter
    private static ViaVersionConfig config;

    public static void setInstance(ViaVersionPlugin plugin) {
        Validate.isTrue(instance == null, "Instance is already set");
        ViaVersion.instance = plugin;
        ViaVersion.config = plugin.getConf();
    }
}
