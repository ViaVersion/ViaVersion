/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.bukkit.commands.BukkitCommandHandler;
import com.viaversion.viaversion.bukkit.commands.BukkitCommandSender;
import com.viaversion.viaversion.bukkit.listeners.JoinListener;
import com.viaversion.viaversion.bukkit.platform.BukkitViaAPI;
import com.viaversion.viaversion.bukkit.platform.BukkitViaConfig;
import com.viaversion.viaversion.bukkit.platform.BukkitViaInjector;
import com.viaversion.viaversion.bukkit.platform.BukkitViaLoader;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTask;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTaskTask;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.unsupported.UnsupportedPlugin;
import com.viaversion.viaversion.unsupported.UnsupportedServerSoftware;
import com.viaversion.viaversion.util.GsonUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ViaVersionPlugin extends JavaPlugin implements ViaPlatform<Player> {
    private static final boolean FOLIA = PaperViaInjector.hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    private static ViaVersionPlugin instance;
    private final BukkitCommandHandler commandHandler;
    private final BukkitViaConfig conf;
    private final ViaAPI<Player> api = new BukkitViaAPI(this);
    private boolean protocolSupport;
    private boolean lateBind;

    public ViaVersionPlugin() {
        instance = this;

        // Command handler
        commandHandler = new BukkitCommandHandler();

        // Init platform
        BukkitViaInjector injector = new BukkitViaInjector();
        Via.init(ViaManagerImpl.builder()
                .platform(this)
                .commandHandler(commandHandler)
                .injector(injector)
                .loader(new BukkitViaLoader(this))
                .build());

        // Config magic
        conf = new BukkitViaConfig();

        // Load a bunch of classes early with slow reflection and more classloading
        if (conf.shouldRegisterUserConnectionOnJoin()) {
            Via.getManager().getScheduler().execute(JoinListener::init);
        }
    }

    @Override
    public void onLoad() {
        protocolSupport = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;
        lateBind = !((BukkitViaInjector) Via.getManager().getInjector()).isBinded();

        if (!lateBind) {
            getLogger().info("ViaVersion " + getDescription().getVersion() + " is now loaded. Registering protocol transformers and injecting...");
            ((ViaManagerImpl) Via.getManager()).init();
        } else {
            getLogger().info("ViaVersion " + getDescription().getVersion() + " is now loaded. Waiting for boot (late-bind).");
        }
    }

    @Override
    public void onEnable() {
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        if (lateBind) {
            getLogger().info("Registering protocol transformers and injecting...");
            manager.init();
        }

        if (FOLIA) {
            // Use Folia's RegionizedServerInitEvent to run code after the server has loaded
            final Class<? extends Event> serverInitEventClass;
            try {
                //noinspection unchecked
                serverInitEventClass = (Class<? extends Event>) Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            getServer().getPluginManager().registerEvent(serverInitEventClass, new Listener() {
            }, EventPriority.HIGHEST, (listener, event) -> manager.onServerLoaded(), this);
        } else if (Via.getManager().getInjector().lateProtocolVersionSetting()) {
            // Enable after server has loaded at the next tick
            runSync(manager::onServerLoaded);
        } else {
            manager.onServerLoaded();
        }

        getCommand("viaversion").setExecutor(commandHandler);
        getCommand("viaversion").setTabCompleter(commandHandler);
    }

    @Override
    public void onDisable() {
        ((ViaManagerImpl) Via.getManager()).destroy();
    }

    @Override
    public String getPlatformName() {
        return Bukkit.getServer().getName();
    }

    @Override
    public String getPlatformVersion() {
        return Bukkit.getServer().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        if (FOLIA) {
            return new BukkitViaTaskTask(Via.getManager().getScheduler().execute(runnable));
        }
        return new BukkitViaTask(getServer().getScheduler().runTaskAsynchronously(this, runnable));
    }

    @Override
    public PlatformTask runRepeatingAsync(final Runnable runnable, final long ticks) {
        if (FOLIA) {
            return new BukkitViaTaskTask(Via.getManager().getScheduler().schedule(runnable, ticks * 50, TimeUnit.MILLISECONDS));
        }
        return new BukkitViaTask(getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, 0, ticks));
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        if (FOLIA) {
            // We just need to make sure everything put here is actually thread safe; currently, this is the case, at least on Folia
            return runAsync(runnable);
        }
        return new BukkitViaTask(getServer().getScheduler().runTask(this, runnable));
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long delay) {
        return new BukkitViaTask(getServer().getScheduler().runTaskLater(this, runnable, delay));
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long period) {
        return new BukkitViaTask(getServer().getScheduler().runTaskTimer(this, runnable, 0, period));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[Bukkit.getOnlinePlayers().size()];
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            array[i++] = new BukkitCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.kickPlayer(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPluginEnabled() {
        return Bukkit.getPluginManager().getPlugin("ViaVersion").isEnabled();
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return conf;
    }

    @Override
    public void onReload() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            getLogger().severe("ViaVersion is already loaded, we're going to kick all the players... because otherwise we'll crash because of ProtocolLib.");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', conf.getReloadDisconnectMsg()));
            }

        } else {
            getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
        }
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins())
            plugins.add(new PluginInfo(p.isEnabled(), p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), p.getDescription().getAuthors()));

        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return !protocolSupport; // Use protocolsupport for older clients
    }

    @Override
    public BukkitViaConfig getConf() {
        return conf;
    }

    @Override
    public ViaAPI<Player> getApi() {
        return api;
    }

    @Override
    public final Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
        final List<UnsupportedSoftware> list = new ArrayList<>(ViaPlatform.super.getUnsupportedSoftwareClasses());
        list.add(new UnsupportedServerSoftware.Builder().name("Yatopia").reason(UnsupportedServerSoftware.Reason.DANGEROUS_SERVER_SOFTWARE)
                .addClassName("org.yatopiamc.yatopia.server.YatopiaConfig")
                .addClassName("net.yatopia.api.event.PlayerAttackEntityEvent")
                .addClassName("yatopiamc.org.yatopia.server.YatopiaConfig") // Only the best kind of software relocates its own classes to hide itself :tinfoilhat:
                .addMethod("org.bukkit.Server", "getLastTickTime").build());
        list.add(new UnsupportedPlugin.Builder().name("software to mess with message signing").reason(UnsupportedPlugin.Reason.SECURE_CHAT_BYPASS)
                .addPlugin("NoEncryption").addPlugin("NoReport")
                .addPlugin("NoChatReports").addPlugin("NoChatReport").build());
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean hasPlugin(final String name) {
        return getServer().getPluginManager().getPlugin(name) != null;
    }

    public boolean isLateBind() {
        return lateBind;
    }

    public boolean isProtocolSupport() {
        return protocolSupport;
    }

    @Deprecated/*(forRemoval = true)*/
    public static ViaVersionPlugin getInstance() {
        return instance;
    }
}
