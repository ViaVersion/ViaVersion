/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.sponge.commands.SpongeCommandHandler;
import com.viaversion.viaversion.sponge.commands.SpongePlayer;
import com.viaversion.viaversion.sponge.platform.SpongeViaAPI;
import com.viaversion.viaversion.sponge.platform.SpongeViaConfig;
import com.viaversion.viaversion.sponge.platform.SpongeViaInjector;
import com.viaversion.viaversion.sponge.platform.SpongeViaLoader;
import com.viaversion.viaversion.sponge.platform.SpongeViaTask;
import com.viaversion.viaversion.sponge.util.LoggerWrapper;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginContributor;

@Plugin("viaversion")
public class SpongePlugin implements ViaPlatform<Player> {

    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().extractUrls().build();
    private final SpongeViaAPI api = new SpongeViaAPI();
    private final PluginContainer container;
    private final Game game;
    @SuppressWarnings("SpongeLogging")
    private final Logger logger;
    private SpongeViaConfig conf;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    SpongePlugin(final PluginContainer container, final Game game, final org.apache.logging.log4j.Logger logger) {
        this.container = container;
        this.game = game;
        this.logger = new LoggerWrapper(logger);
    }

    @Listener
    public void constructPlugin(ConstructPluginEvent event) {
        // Setup Plugin
        conf = new SpongeViaConfig(configDir.toFile());

        // Init platform
        Via.init(ViaManagerImpl.builder()
            .platform(this)
            .commandHandler(new SpongeCommandHandler())
            .injector(new SpongeViaInjector())
            .loader(new SpongeViaLoader(this))
            .build());
        conf.reload();
    }

    @Listener
    public void onServerStart(StartingEngineEvent<Server> event) {
        // Can't use the command register event for raw commands...
        Sponge.server().commandManager().registrar(Command.Raw.class).get().register(container, (Command.Raw) Via.getManager().getCommandHandler(), "viaversion", "viaver", "vvsponge");

        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        manager.init();
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        manager.onServerLoaded();
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        ((ViaManagerImpl) Via.getManager()).destroy();
    }

    @Override
    public String getPlatformName() {
        return game.platform().container(Platform.Component.IMPLEMENTATION).metadata().name().orElse("unknown");
    }

    @Override
    public String getPlatformVersion() {
        return game.platform().container(Platform.Component.IMPLEMENTATION).metadata().version().toString();
    }

    @Override
    public String getPluginVersion() {
        return container.metadata().version().toString();
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        final Task task = Task.builder().plugin(container).execute(runnable).build();
        return new SpongeViaTask(game.asyncScheduler().submit(task));
    }

    @Override
    public PlatformTask runRepeatingAsync(final Runnable runnable, final long ticks) {
        final Task task = Task.builder().plugin(container).execute(runnable).interval(Ticks.of(ticks)).build();
        return new SpongeViaTask(game.asyncScheduler().submit(task));
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        final Task task = Task.builder().plugin(container).execute(runnable).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long delay) {
        final Task task = Task.builder().plugin(container).execute(runnable).delay(Ticks.of(delay)).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long period) {
        final Task task = Task.builder().plugin(container).execute(runnable).interval(Ticks.of(period)).build();
        return new SpongeViaTask(game.server().scheduler().submit(task));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        Collection<ServerPlayer> players = game.server().onlinePlayers();
        ViaCommandSender[] array = new ViaCommandSender[players.size()];
        int i = 0;
        for (ServerPlayer player : players) {
            array[i++] = new SpongePlayer(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        game.server().player(uuid).ifPresent(player -> player.sendMessage(LEGACY_SERIALIZER.deserialize(message)));
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return game.server().player(uuid).map(player -> {
            player.kick(LegacyComponentSerializer.legacySection().deserialize(message));
            return true;
        }).orElse(false);
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public File getDataFolder() {
        return configDir.toFile();
    }

    @Override
    public void onReload() {
        logger.severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (PluginContainer plugin : game.pluginManager().plugins()) {
            PluginMetadata metadata = plugin.metadata();
            plugins.add(new PluginInfo(
                true,
                metadata.name().orElse("Unknown"),
                metadata.version().toString(),
                plugin.instance() != null ? plugin.instance().getClass().getCanonicalName() : "Unknown",
                metadata.contributors().stream().map(PluginContributor::name).collect(Collectors.toList())
            ));
        }
        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public boolean hasPlugin(final String name) {
        return game.pluginManager().plugin(name).isPresent();
    }

    @Override
    public SpongeViaAPI getApi() {
        return api;
    }

    @Override
    public SpongeViaConfig getConf() {
        return conf;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public PluginContainer container() {
        return container;
    }

}
