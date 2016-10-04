package us.myles.ViaVersion.protocols.base;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

public class VersionProvider implements Provider {

    public int getServerProtocol(UserConnection connection) throws Exception {
        return ProtocolRegistry.SERVER_PROTOCOL;
    }
}
