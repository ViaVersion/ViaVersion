package us.myles.ViaVersion.velocity.platform;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;

public class VelocityConnectionManager extends ViaConnectionManager {
    @Override
    public boolean isFrontEnd(UserConnection conn) {
        return !conn.isClientSide();
    }
}
