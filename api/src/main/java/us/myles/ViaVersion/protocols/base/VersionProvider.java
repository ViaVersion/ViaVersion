package us.myles.ViaVersion.protocols.base;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;

public interface VersionProvider extends Provider {

    int getServerProtocol(UserConnection connection) throws Exception;
}
