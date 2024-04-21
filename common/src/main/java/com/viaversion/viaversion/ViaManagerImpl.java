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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaManager;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.api.scheduler.Scheduler;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.configuration.ConfigurationProviderImpl;
import com.viaversion.viaversion.connection.ConnectionManagerImpl;
import com.viaversion.viaversion.debug.DebugHandlerImpl;
import com.viaversion.viaversion.protocol.ProtocolManagerImpl;
import com.viaversion.viaversion.protocol.ServerProtocolVersionRange;
import com.viaversion.viaversion.protocol.ServerProtocolVersionSingleton;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.task.TabCompleteThread;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ViaIdleThread;
import com.viaversion.viaversion.scheduler.TaskScheduler;
import com.viaversion.viaversion.update.UpdateUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViaManagerImpl implements ViaManager {
    private final ProtocolManagerImpl protocolManager = new ProtocolManagerImpl();
    private final ConnectionManager connectionManager = new ConnectionManagerImpl();
    private final ConfigurationProvider configurationProvider = new ConfigurationProviderImpl();
    private final DebugHandler debugHandler = new DebugHandlerImpl();
    private final ViaProviders providers = new ViaProviders();
    private final Scheduler scheduler = new TaskScheduler();
    private final ViaPlatform<?> platform;
    private final ViaInjector injector;
    private final ViaCommandHandler commandHandler;
    private final ViaPlatformLoader loader;
    private final Set<String> subPlatforms = new HashSet<>();
    private List<Runnable> enableListeners = new ArrayList<>();
    private PlatformTask<?> mappingLoadingTask;
    private boolean initialized;

    public ViaManagerImpl(ViaPlatform<?> platform, ViaInjector injector, ViaCommandHandler commandHandler, ViaPlatformLoader loader) {
        this.platform = platform;
        this.injector = injector;
        this.commandHandler = commandHandler;
        this.loader = loader;
    }

    public static ViaManagerBuilder builder() {
        return new ViaManagerBuilder();
    }

    public void init() {
        configurationProvider.register(platform.getConf());

        if (System.getProperty("ViaVersion") != null) {
            // Reload?
            platform.onReload();
        }

        // Load supported protocol versions if we can
        if (!injector.lateProtocolVersionSetting()) {
            loadServerProtocol();
        }

        MappingDataLoader.loadGlobalIdentifiers();

        // Register protocols
        protocolManager.registerProtocols();

        // Inject
        try {
            injector.inject();
        } catch (Exception e) {
            platform.getLogger().log(Level.SEVERE, "ViaVersion failed to inject:", e);
            return;
        }

        // Mark as injected
        System.setProperty("ViaVersion", platform.getPluginVersion());

        for (Runnable listener : enableListeners) {
            listener.run();
        }
        enableListeners = null;

        initialized = true;
    }

    public void onServerLoaded() {
        // Check for updates
        if (platform.getConf().isCheckForUpdates()) {
            UpdateUtil.sendUpdateMessage();
        }

        if (!protocolManager.getServerProtocolVersion().isKnown()) {
            // Try again
            loadServerProtocol();
        }

        // Check if there are any pipes to this version
        ServerProtocolVersion protocolVersion = protocolManager.getServerProtocolVersion();
        if (protocolVersion.isKnown()) {
            if (platform.isProxy()) {
                platform.getLogger().info("ViaVersion detected lowest supported version by the proxy: " + protocolVersion.lowestSupportedProtocolVersion());
                platform.getLogger().info("Highest supported version by the proxy: " + protocolVersion.highestSupportedProtocolVersion());
                if (debugHandler.enabled()) {
                    platform.getLogger().info("Supported version range: " + Arrays.toString(protocolVersion.supportedProtocolVersions().toArray(new ProtocolVersion[0])));
                }
            } else {
                platform.getLogger().info("ViaVersion detected server version: " + protocolVersion.highestSupportedProtocolVersion());
            }

            if (!protocolManager.isWorkingPipe()) {
                platform.getLogger().warning("ViaVersion does not have any compatible versions for this server version!");
                platform.getLogger().warning("Please remember that ViaVersion only adds support for versions newer than the server version.");
                platform.getLogger().warning("If you need support for older versions you may need to use one or more ViaVersion addons too.");
                platform.getLogger().warning("In that case please read the ViaVersion resource page carefully or use https://viaversion.com/setup");
                platform.getLogger().warning("and if you're still unsure, feel free to join our Discord-Server for further assistance.");
            } else if (protocolVersion.highestSupportedProtocolVersion().olderThan(ProtocolVersion.v1_13)) {
                platform.getLogger().warning("This version of Minecraft is extremely outdated and support for it has reached its end of life. "
                        + "You will still be able to run Via on this Minecraft version, but we are unlikely to provide any further fixes or help with problems specific to legacy Minecraft versions. "
                        + "Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
            }
        }

        checkJavaVersion();

        // Check for unsupported plugins/software
        unsupportedSoftwareWarning();

        // Load Platform
        loader.load();
        // Common tasks
        mappingLoadingTask = Via.getPlatform().runRepeatingAsync(() -> {
            if (protocolManager.checkForMappingCompletion() && mappingLoadingTask != null) {
                mappingLoadingTask.cancel();
                mappingLoadingTask = null;
            }
        }, 10L);

        final ProtocolVersion serverProtocolVersion = protocolManager.getServerProtocolVersion().lowestSupportedProtocolVersion();
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_9)) {
            if (Via.getConfig().isSimulatePlayerTick()) {
                Via.getPlatform().runRepeatingSync(new ViaIdleThread(), 1L);
            }
        }
        if (serverProtocolVersion.olderThan(ProtocolVersion.v1_13)) {
            if (Via.getConfig().get1_13TabCompleteDelay() > 0) {
                Via.getPlatform().runRepeatingSync(new TabCompleteThread(), 1L);
            }
        }

        // Refresh Versions
        protocolManager.refreshVersions();
    }

    private void loadServerProtocol() {
        try {
            ProtocolVersion serverProtocolVersion = injector.getServerProtocolVersion();
            ServerProtocolVersion versionInfo;
            if (platform.isProxy()) {
                SortedSet<ProtocolVersion> supportedVersions = injector.getServerProtocolVersions();
                versionInfo = new ServerProtocolVersionRange(supportedVersions.first(), supportedVersions.last(), supportedVersions);
            } else {
                versionInfo = new ServerProtocolVersionSingleton(serverProtocolVersion);
            }

            protocolManager.setServerProtocol(versionInfo);
        } catch (Exception e) {
            platform.getLogger().log(Level.SEVERE, "ViaVersion failed to get the server protocol!", e);
        }
    }

    public void destroy() {
        // Uninject
        platform.getLogger().info("ViaVersion is disabling, if this is a reload and you experience issues consider rebooting.");
        try {
            injector.uninject();
        } catch (Exception e) {
            platform.getLogger().log(Level.SEVERE, "ViaVersion failed to uninject:", e);
        }

        loader.unload();
        scheduler.shutdown();
    }

    private void checkJavaVersion() { // Stolen from Paper
        String javaVersion = System.getProperty("java.version");
        Matcher matcher = Pattern.compile("(?:1\\.)?(\\d+)").matcher(javaVersion);
        if (!matcher.find()) {
            platform.getLogger().warning("Failed to determine Java version; could not parse: " + javaVersion);
            return;
        }

        String versionString = matcher.group(1);
        int version;
        try {
            version = Integer.parseInt(versionString);
        } catch (NumberFormatException e) {
            platform.getLogger().log(Level.WARNING, "Failed to determine Java version; could not parse: " + versionString, e);
            return;
        }

        if (version < 17) {
            platform.getLogger().warning("You are running an outdated Java version, please update it to at least Java 17 (your version is " + javaVersion + ").");
            platform.getLogger().warning("Starting with Minecraft 1.21, ViaVersion will no longer officially be compatible with this version of Java, only offering unsupported compatibility builds.");
        }
    }

    private void unsupportedSoftwareWarning() {
        boolean found = false;
        for (final UnsupportedSoftware software : platform.getUnsupportedSoftwareClasses()) {
            final String match = software.match();
            if (match == null) {
                continue;
            }

            // Found something
            if (!found) {
                platform.getLogger().severe("************************************************");
                platform.getLogger().severe("You are using unsupported software and may encounter unforeseeable issues.");
                platform.getLogger().severe("");
                found = true;
            }

            platform.getLogger().severe("We strongly advise against using " + match + ":");
            platform.getLogger().severe(software.getReason());
            platform.getLogger().severe("");
        }

        if (found) {
            platform.getLogger().severe("We will not provide support in case you encounter issues possibly related to this software.");
            platform.getLogger().severe("************************************************");
        }
    }

    @Override
    public ViaPlatform<?> getPlatform() {
        return platform;
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    @Override
    public ViaProviders getProviders() {
        return providers;
    }

    @Override
    public DebugHandler debugHandler() {
        return debugHandler;
    }

    @Override
    public ViaInjector getInjector() {
        return injector;
    }

    @Override
    public ViaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public ViaPlatformLoader getLoader() {
        return loader;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    /**
     * Returns a mutable set of self-added subplatform version strings.
     * This set is expanded by the subplatform itself (e.g. ViaBackwards), and may not contain all running ones.
     *
     * @return mutable set of subplatform versions
     */
    public Set<String> getSubPlatforms() {
        return subPlatforms;
    }

    /**
     * Adds a runnable to be executed when ViaVersion has finished its init before the full server load.
     *
     * @param runnable runnable to be executed
     */
    public void addEnableListener(Runnable runnable) {
        enableListeners.add(runnable);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public static final class ViaManagerBuilder {
        private ViaPlatform<?> platform;
        private ViaInjector injector;
        private ViaCommandHandler commandHandler;
        private ViaPlatformLoader loader;

        public ViaManagerBuilder platform(ViaPlatform<?> platform) {
            this.platform = platform;
            return this;
        }

        public ViaManagerBuilder injector(ViaInjector injector) {
            this.injector = injector;
            return this;
        }

        public ViaManagerBuilder loader(ViaPlatformLoader loader) {
            this.loader = loader;
            return this;
        }

        public ViaManagerBuilder commandHandler(ViaCommandHandler commandHandler) {
            this.commandHandler = commandHandler;
            return this;
        }

        public ViaManagerImpl build() {
            return new ViaManagerImpl(platform, injector, commandHandler, loader);
        }
    }
}
