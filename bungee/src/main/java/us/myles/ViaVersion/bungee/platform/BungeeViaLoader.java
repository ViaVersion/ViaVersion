package us.myles.ViaVersion.bungee.platform;

import lombok.AllArgsConstructor;
import us.myles.ViaVersion.BungeePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.bungee.providers.BungeeMovementTransmitter;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class BungeeViaLoader implements ViaPlatformLoader {
    private BungeePlugin plugin;

    @Override
    public void load() {
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());

        plugin.getProxy().getScheduler().schedule(plugin, new ProtocolDetectorService(plugin), 0, 1, TimeUnit.MINUTES);
    }
}
