/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.platform;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.VersionInfo;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * ViaPlatform represents a platform ViaVersion runs on
 *
 * @param <T> - The player type for the platform, used for API related methods
 */
public interface ViaPlatform<T> {

    /**
     * Get the logger for this platform
     *
     * @return Java Logger (may be a wrapper)
     */
    Logger getLogger();

    /**
     * Creates a logger for the given name. Returns the main platform logger by default
     *
     * @param name The name of the logger
     * @return The logger
     */
    default Logger createLogger(String name) {
        return getLogger();
    }

    /**
     * Get the platform name
     *
     * @return Platform Name (simply its name)
     */
    String getPlatformName();

    /**
     * Get the platform version
     *
     * @return Platform version
     */
    String getPlatformVersion();

    /**
     * Returns true if the server Via is running on is a proxy server.
     *
     * @return true if the platform is a proxy
     */
    default boolean isProxy() {
        return false;
    }

    /**
     * Get the plugin version.
     *
     * @return Plugin version as a semver string
     */
    default String getPluginVersion() {
        return VersionInfo.getVersion();
    }

    /**
     * Run a task Async
     *
     * @param runnable The task to run
     * @return The Task ID
     */
    default PlatformTask runAsync(Runnable runnable) {
        return new ViaPlatformTask(Via.getManager().getScheduler().execute(runnable));
    }

    /**
     * Run a task async at a repeating interval.
     *
     * @param runnable The task to run
     * @param ticks    The interval to run it at
     * @return The Task ID
     */
    default PlatformTask runRepeatingAsync(Runnable runnable, long ticks) {
        return new ViaPlatformTask(Via.getManager().getScheduler().scheduleRepeating(runnable, ticks * 50, ticks * 50, TimeUnit.MILLISECONDS));
    }

    /**
     * Run a task Sync
     *
     * @param runnable The task to run
     * @return The Task ID
     */
    default PlatformTask runSync(Runnable runnable) {
        return runAsync(runnable);
    }

    /**
     * Runs a synchronous task after a delay in ticks.
     *
     * @param runnable task to run
     * @param delay    delay in ticks to run it after
     * @return created task
     */
    default PlatformTask runSync(Runnable runnable, long delay) {
        return new ViaPlatformTask(Via.getManager().getScheduler().schedule(runnable, delay * 50, TimeUnit.MILLISECONDS));
    }

    /**
     * Runs a synchronous task at a repeating interval.
     *
     * @param runnable task to run
     * @param period   period in ticks to run at
     * @return created task
     */
    default PlatformTask runRepeatingSync(Runnable runnable, long period) {
        return runRepeatingAsync(runnable, period);
    }

    /**
     * Send a message to a player
     *
     * @param connection The UserConnection
     * @param message    The message to send
     */
    default void sendMessage(UserConnection connection, String message) {
        // To be overridden if needed
    }

    /**
     * Kick a player for a reason
     *
     * @param connection The UserConnection
     * @param message    The message to kick them with
     * @return True if it was successful
     */
    default boolean kickPlayer(UserConnection connection, String message) {
        // To be overridden if needed
        return false;
    }

    /**
     * Send a custom payload to from a player to the server.
     *
     * @param connection The UserConnection
     * @param channel    The channel to send the payload on
     * @param message    The data to send
     */
    default void sendCustomPayload(UserConnection connection, String channel, byte[] message) {
        // To be overridden if needed
    }

    /**
     * Send a custom payload from the server to a player.
     *
     * @param connection The UserConnection
     * @param channel    The channel to send the payload on
     * @param message    The data to send
     */
    default void sendCustomPayloadToClient(UserConnection connection, String channel, byte[] message) {
        // To be overridden if needed
    }

    /**
     * Get the API for this platform
     *
     * @return The API for the platform
     */
    ViaAPI<T> getApi();

    /**
     * Get the config API for this platform
     *
     * @return The config API
     */
    ViaVersionConfig getConf();

    /**
     * Get ViaVersions's data folder.
     *
     * @return data folder
     */
    File getDataFolder();

    /**
     * Called when ViaVersion is initialized twice during the same process, this happens on platforms where ViaVersion
     * is loaded as a plugin which can be reloaded.
     */
    default void onReload() {
        // To be overridden if needed
    }

    /**
     * Gets optional platform specific data for /viaversion dump. This can be a specific version or a list of installed
     * plugins on the platform.
     *
     * @return The json data
     */
    default JsonObject getDump() {
        return new JsonObject();
    }

    /**
     * Returns an immutable collection of classes to be checked as unsupported software with their software name.
     * If any of the classes exist at runtime, a warning about their potential instability will be given to the console.
     *
     * @return immutable collection of unsupported software to be checked
     */
    default Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
        return List.of();
    }

    /**
     * Returns whether the platform has a plugin/mod with the given name (even if disabled).
     *
     * @param name plugin or identifier
     * @return whether the platform has a plugin/mod with the given name
     */
    default boolean hasPlugin(String name) {
        return false;
    }

    /**
     * Returns whether the platform might be reloading.
     *
     * @return whether the platform might be reloading
     */
    default boolean couldBeReloading() {
        return false;
    }
}
