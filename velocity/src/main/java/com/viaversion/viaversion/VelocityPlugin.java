/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaServerProxyPlatform;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.VersionInfo;
import com.viaversion.viaversion.velocity.command.VelocityCommandHandler;
import com.viaversion.viaversion.velocity.platform.VelocityViaAPI;
import com.viaversion.viaversion.velocity.platform.VelocityViaConfig;
import com.viaversion.viaversion.velocity.platform.VelocityViaInjector;
import com.viaversion.viaversion.velocity.platform.VelocityViaLoader;
import com.viaversion.viaversion.velocity.platform.VelocityViaTask;
import com.viaversion.viaversion.velocity.service.ProtocolDetectorService;
import com.viaversion.viaversion.velocity.util.LoggerWrapper;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

@Plugin(
    id = "viaversion",
    name = "ViaVersion",
    version = VersionInfo.VERSION,
    authors = {"_MylesC", "creeper123123321", "Gerrygames", "kennytv", "Matsv", "EnZaXD", "RK_01"},
    description = "Allows the connection of newer clients to older server versions for Minecraft servers.",
    url = "https://viaversion.com"
)
public class VelocityPlugin implements ViaServerProxyPlatform<Player> {
    public static final LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character(ChatColorUtil.COLOR_CHAR).extractUrls().build();
    public static ProxyServer PROXY;

    @Inject
    private ProxyServer proxy;
    @Inject
    private Logger loggerslf4j;
    @Inject
    @DataDirectory
    private Path configDir;

    private final ProtocolDetectorService protocolDetectorService = new ProtocolDetectorService();
    private VelocityViaAPI api;
    private java.util.logging.Logger logger;
    private VelocityViaConfig conf;

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        if (!hasConnectionEvent()) {
            // No way to disable the plugin :(
            Logger logger = this.loggerslf4j;
            logger.error("      / \\");
            logger.error("     /   \\");
            logger.error("    /  |  \\");
            logger.error("   /   |   \\        VELOCITY 3.0.0 IS REQUIRED");
            logger.error("  /         \\   VIAVERSION WILL NOT WORK AS INTENDED");
            logger.error(" /     o     \\");
            logger.error("/_____________\\");
        }

        PROXY = proxy;
        VelocityCommandHandler commandHandler = new VelocityCommandHandler();
        PROXY.getCommandManager().register("viaver", commandHandler, "vvvelocity", "viaversion");
        api = new VelocityViaAPI();
        logger = new LoggerWrapper(loggerslf4j);
        conf = new VelocityViaConfig(configDir.toFile(), logger);
        Via.init(ViaManagerImpl.builder()
            .platform(this)
            .commandHandler(commandHandler)
            .loader(new VelocityViaLoader())
            .injector(new VelocityViaInjector()).build());
        conf.reload();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyLateInit(ProxyInitializeEvent e) {
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        manager.init();
        manager.onServerLoaded();
    }

    @Override
    public String getPlatformName() {
        String proxyImpl = ProxyServer.class.getPackage().getImplementationTitle();
        return (proxyImpl != null) ? proxyImpl : "Velocity";
    }

    @Override
    public String getPlatformVersion() {
        String version = ProxyServer.class.getPackage().getImplementationVersion();
        return (version != null) ? version : "Unknown";
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        return runSync(runnable);
    }

    @Override
    public PlatformTask runRepeatingAsync(final Runnable runnable, final long ticks) {
        return new VelocityViaTask(
            PROXY.getScheduler()
                .buildTask(this, runnable)
                .repeat(ticks * 50, TimeUnit.MILLISECONDS).schedule()
        );
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        return runSync(runnable, 0L);
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long delay) {
        return new VelocityViaTask(
            PROXY.getScheduler()
                .buildTask(this, runnable)
                .delay(delay * 50, TimeUnit.MILLISECONDS).schedule()
        );
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long period) {
        return runRepeatingAsync(runnable, period);
    }

    @Override
    public void sendMessage(UserConnection connection, String message) {
        final UUID uuid = connection.getProtocolInfo().getUuid();
        PROXY.getPlayer(uuid).ifPresent(player -> player.sendMessage(COMPONENT_SERIALIZER.deserialize(message)));
    }

    @Override
    public void sendCustomPayload(final UserConnection connection, final String channel, final byte[] message) {
        final UUID uuid = connection.getProtocolInfo().getUuid();
        PROXY.getPlayer(uuid).flatMap(Player::getCurrentServer).ifPresent(server -> server.sendPluginMessage(MinecraftChannelIdentifier.from(channel), message));
    }

    @Override
    public void sendCustomPayloadToClient(final UserConnection connection, final String channel, final byte[] message) {
        final UUID uuid = connection.getProtocolInfo().getUuid();
        PROXY.getPlayer(uuid).ifPresent(player -> player.sendPluginMessage(MinecraftChannelIdentifier.from(channel), message));
    }

    @Override
    public boolean kickPlayer(UserConnection connection, String message) {
        final UUID uuid = connection.getProtocolInfo().getUuid();
        if (uuid == null) {
            return false;
        }
        return PROXY.getPlayer(uuid).map(it -> {
            it.disconnect(LegacyComponentSerializer.legacySection().deserialize(message));
            return true;
        }).orElse(false);
    }

    @Override
    public File getDataFolder() {
        return configDir.toFile();
    }

    @Override
    public VelocityViaAPI getApi() {
        return api;
    }

    @Override
    public VelocityViaConfig getConf() {
        return conf;
    }

    @Override
    public JsonObject getDump() {
        JsonObject extra = new JsonObject();
        List<PluginInfo> plugins = new ArrayList<>();
        for (PluginContainer p : PROXY.getPluginManager().getPlugins()) {
            plugins.add(new PluginInfo(
                true,
                p.getDescription().getName().orElse(p.getDescription().getId()),
                p.getDescription().getVersion().orElse("Unknown Version"),
                p.getInstance().map(instance -> instance.getClass().getCanonicalName()).orElse("Unknown"),
                p.getDescription().getAuthors()
            ));
        }
        extra.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
        extra.add("servers", GsonUtil.getGson().toJsonTree(protocolDetectorService.detectedProtocolVersions()));
        return extra;
    }

    @Override
    public boolean hasPlugin(final String name) {
        return proxy.getPluginManager().getPlugin(name).isPresent();
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return logger;
    }

    @Override
    public ProtocolDetectorService protocolDetectorService() {
        return protocolDetectorService;
    }

    private boolean hasConnectionEvent() {
        try {
            Class.forName("com.velocitypowered.proxy.protocol.VelocityConnectionEvent");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
