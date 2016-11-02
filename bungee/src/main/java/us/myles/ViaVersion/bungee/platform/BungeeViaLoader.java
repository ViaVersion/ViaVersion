package us.myles.ViaVersion.bungee.platform;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import us.myles.ViaVersion.BungeePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.bungee.handlers.BungeeServerHandler;
import us.myles.ViaVersion.bungee.listeners.UpdateListener;
import us.myles.ViaVersion.bungee.providers.BungeeMovementTransmitter;
import us.myles.ViaVersion.bungee.providers.BungeeVersionProvider;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class BungeeViaLoader implements ViaPlatformLoader {
    private BungeePlugin plugin;

    @Override
    public void load() {
        // Listeners
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, plugin);
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new UpdateListener());
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new BungeeServerHandler());

        // Providers
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
        Via.getManager().getProviders().use(VersionProvider.class, new BungeeVersionProvider());
        if (plugin.getConf().getBungeePingInterval() > 0) {
            plugin.getProxy().getScheduler().schedule(plugin, new ProtocolDetectorService(plugin), 0, plugin.getConf().getBungeePingInterval(), TimeUnit.SECONDS);
        }
    }
}
