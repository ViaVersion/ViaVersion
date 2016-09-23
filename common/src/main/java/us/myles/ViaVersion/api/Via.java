package us.myles.ViaVersion.api;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import us.myles.ViaVersion.api.platform.ViaPlatform;

public class Via {
    @Getter
    private static ViaPlatform platform;

    public static void init(ViaPlatform platform) {
        Validate.isTrue(platform == null, "Platform is already set");
        Via.platform = platform;
    }
}
