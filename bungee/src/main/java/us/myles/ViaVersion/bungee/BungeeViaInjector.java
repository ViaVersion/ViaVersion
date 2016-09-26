package us.myles.ViaVersion.bungee;

import us.myles.ViaVersion.api.platform.ViaInjector;

public class BungeeViaInjector implements ViaInjector {
    @Override
    public void inject() throws Exception {

    }

    @Override
    public void uninject() throws Exception {

    }

    @Override
    public int getServerProtocolVersion() throws Exception {
        return 47;
    }
}
