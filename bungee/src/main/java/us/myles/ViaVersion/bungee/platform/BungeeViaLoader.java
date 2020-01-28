package us.myles.ViaVersion.bungee.platform;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import us.myles.ViaVersion.BungeePlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.bungee.handlers.BungeeServerHandler;
import us.myles.ViaVersion.bungee.listeners.ElytraPatch;
import us.myles.ViaVersion.bungee.listeners.UpdateListener;
import us.myles.ViaVersion.bungee.providers.*;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BungeeViaLoader implements ViaPlatformLoader {
    private final BungeePlugin plugin;

    private final Set<Listener> listeners = new HashSet<>();
    private final Set<ScheduledTask> tasks = new HashSet<>();

    public BungeeViaLoader(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    private void registerListener(Listener listener) {
        listeners.add(listener);
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, listener);
    }

    @Override
    public void load() {
        // Listeners
        registerListener(plugin);
        registerListener(new UpdateListener());
        registerListener(new BungeeServerHandler());

        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            registerListener(new ElytraPatch());
        }

        // Providers
        Via.getManager().getProviders().use(VersionProvider.class, new BungeeVersionProvider());
        Via.getManager().getProviders().use(EntityIdProvider.class, new BungeeEntityIdProvider());

        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
            Via.getManager().getProviders().use(BossBarProvider.class, new BungeeBossBarProvider());
            Via.getManager().getProviders().use(MainHandProvider.class, new BungeeMainHandProvider());
        }

        if (plugin.getConf().getBungeePingInterval() > 0) {
            tasks.add(plugin.getProxy().getScheduler().schedule(
                    plugin,
                    new ProtocolDetectorService(plugin),
                    0, plugin.getConf().getBungeePingInterval(),
                    TimeUnit.SECONDS
            ));
        }
    }

    @Override
    public void unload() {
        for (Listener listener : listeners) {
            ProxyServer.getInstance().getPluginManager().unregisterListener(listener);
        }
        listeners.clear();
        for (ScheduledTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
}
