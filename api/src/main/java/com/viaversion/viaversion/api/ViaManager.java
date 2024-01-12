/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api;

import com.viaversion.viaversion.api.command.ViaVersionCommand;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.scheduler.Scheduler;
import java.util.Set;

public interface ViaManager {

    /**
     * Returns the protocol manager for registering/getting protocols and their mapping data loading.
     *
     * @return protocol manager
     */
    ProtocolManager getProtocolManager();

    /**
     * Returns platform for handling platform specific configuration, tasks, and plugin data.
     *
     * @return platform
     */
    ViaPlatform<?> getPlatform();

    /**
     * Returns the userconnection manager for handling clients connected to the server.
     *
     * @return userconnection manager
     */
    ConnectionManager getConnectionManager();

    /**
     * Returns the manager for Via providers.
     *
     * @return provider manager
     */
    ViaProviders getProviders();

    /**
     * Returns the injector for injecting netty handlers and initially getting the server protocol version.
     *
     * @return injector
     */
    ViaInjector getInjector();

    /**
     * Returns the command handler for managing ViaVersion subcommands.
     *
     * @return command handler
     */
    ViaVersionCommand getCommandHandler();

    /**
     * Returns the platform loader responsible for registering listeners, providers and such.
     *
     * @return platform loader
     */
    ViaPlatformLoader getLoader();

    /**
     * Returns the async task scheduler.
     *
     * @return async task scheduler
     */
    Scheduler getScheduler();

    /**
     * Returns the configuration provider.
     *
     * @return the configuration provider
     */
    ConfigurationProvider getConfigurationProvider();

    /**
     * If debug is enabled, packets and other otherwise suppressed warnings will be logged.
     *
     * @return true if enabled
     */
    default boolean isDebug() {
        return debugHandler().enabled();
    }

    /**
     * Sets the debug mode. If enabled, packets and other otherwise suppressed warnings will be logged.
     *
     * @param debug whether debug should be enabled
     */
    @Deprecated
    default void setDebug(boolean debug) {
        debugHandler().setEnabled(debug);
    }

    /**
     * Returns the debug handler.
     *
     * @return debug handler
     */
    DebugHandler debugHandler();

    /**
     * Returns a mutable set of self-added subplatform version strings.
     * This set is expanded by the subplatform itself (e.g. ViaBackwards), and may not contain all running ones.
     *
     * @return mutable set of subplatform versions
     */
    Set<String> getSubPlatforms();

    /**
     * Adds a runnable to be executed when ViaVersion has finished its init before the full server load.
     *
     * @param runnable runnable to be executed
     */
    void addEnableListener(Runnable runnable);

    /**
     * Returns whether the manager has been initialized (and protocols have been loaded).
     *
     * @return whether the manager has been initialized
     */
    boolean isInitialized();

}
