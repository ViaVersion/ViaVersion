package us.myles.ViaVersion.velocity.platform;

import com.velocitypowered.api.plugin.PluginContainer;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.VersionProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import us.myles.ViaVersion.velocity.handlers.VelocityServerHandler;
import us.myles.ViaVersion.velocity.listeners.ElytraPatch;
import us.myles.ViaVersion.velocity.listeners.UpdateListener;
import us.myles.ViaVersion.velocity.providers.VelocityBossBarProvider;
import us.myles.ViaVersion.velocity.providers.VelocityMovementTransmitter;
import us.myles.ViaVersion.velocity.providers.VelocityVersionProvider;
import us.myles.ViaVersion.velocity.service.ProtocolDetectorService;

public class VelocityViaLoader implements ViaPlatformLoader {
    @Override
    public void load() {
        Object plugin = VelocityPlugin.PROXY.getPluginManager()
                .getPlugin("viaversion").flatMap(PluginContainer::getInstance).get();

        if (ProtocolRegistry.SERVER_PROTOCOL < ProtocolVersion.v1_9.getId()) {
            Via.getManager().getProviders().use(MovementTransmitterProvider.class, new VelocityMovementTransmitter());
            Via.getManager().getProviders().use(BossBarProvider.class, new VelocityBossBarProvider());
            VelocityPlugin.PROXY.getEventManager().register(plugin, new ElytraPatch());
        }

        Via.getManager().getProviders().use(VersionProvider.class, new VelocityVersionProvider());
        // We probably don't need a EntityIdProvider because velocity sends a Join packet on server change
        // We don't need main hand patch because Join Game packet makes client send hand data again

        VelocityPlugin.PROXY.getEventManager().register(plugin, new UpdateListener());
        VelocityPlugin.PROXY.getEventManager().register(plugin, new VelocityServerHandler());

        int pingInterval = ((VelocityViaConfig) Via.getPlatform().getConf()).getVelocityPingInterval();
        if (pingInterval > 0) {
            Via.getPlatform().runRepeatingSync(
                    new ProtocolDetectorService(),
                    pingInterval * 20L);
        }
    }

    @Override
    public void unload() {
        // Probably not useful, there's no ProxyReloadEvent
    }
}
