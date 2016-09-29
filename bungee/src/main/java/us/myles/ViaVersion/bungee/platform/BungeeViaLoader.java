package us.myles.ViaVersion.bungee.platform;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.bungee.providers.BungeeMovementTransmitter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

public class BungeeViaLoader implements ViaPlatformLoader {
    @Override
    public void load() {
        // TODO: Config
        // TODO: Platform specific commands
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
    }
}
