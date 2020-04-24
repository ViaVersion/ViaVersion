package us.myles.ViaVersion.api;

import com.google.common.base.Preconditions;
import us.myles.ViaVersion.ViaManager;
import us.myles.ViaVersion.api.platform.ViaPlatform;

public class Via {
    private static ViaPlatform platform;
    private static ViaManager manager;

    /**
     * Register the ViaManager associated with the platform.
     *
     * @param viaManager The ViaManager
     */
    public static void init(ViaManager viaManager) {
        Preconditions.checkArgument(manager == null, "ViaManager is already set");

        Via.platform = viaManager.getPlatform();
        Via.manager = viaManager;
    }

    /**
     * Get the API associated with the current platform.
     *
     * @return API instance
     */
    public static ViaAPI getAPI() {
        Preconditions.checkArgument(platform != null, "ViaVersion has not loaded the Platform");
        return Via.platform.getApi();
    }

    /**
     * Get the config associated with the current platform.
     *
     * @return Config instance
     */
    public static ViaVersionConfig getConfig() {
        Preconditions.checkArgument(platform != null, "ViaVersion has not loaded the Platform");
        return Via.platform.getConf();
    }

    public static ViaPlatform getPlatform() {
        return platform;
    }

    public static ViaManager getManager() {
        return manager;
    }
}
