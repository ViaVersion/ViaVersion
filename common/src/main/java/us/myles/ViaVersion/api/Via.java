package us.myles.ViaVersion.api;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import us.myles.ViaVersion.ViaManager;
import us.myles.ViaVersion.api.platform.ViaPlatform;

public class Via {
    @Getter
    private static ViaPlatform platform;
    @Getter
    private static ViaManager manager;

    public static void init(ViaPlatform platform) {
        Validate.isTrue(platform == null, "Platform is already set");
        Via.platform = platform;
        Via.manager = new ViaManager(platform);
    }

    public static ViaAPI getAPI() {
        Validate.isTrue(platform != null, "ViaVersion has not loaded the Platform");
        return Via.platform.getApi();
    }

    public static ViaVersionConfig getConfig() {
        Validate.isTrue(platform != null, "ViaVersion has not loaded the Platform");
        return Via.platform.getConf();
    }
}
