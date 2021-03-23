/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.command.ViaVersionCommand;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.api.platform.ViaPlatform;
import us.myles.ViaVersion.api.platform.ViaPlatformLoader;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ViaManager {

    Set<UserConnection> getConnections();

    /**
     * @deprecated use getConnectedClients()
     */
    @Deprecated
    Map<UUID, UserConnection> getPortedPlayers();

    Map<UUID, UserConnection> getConnectedClients();

    UUID getConnectedClientId(UserConnection conn);

    /**
     * @see ViaConnectionManager#isClientConnected(UUID)
     */
    boolean isClientConnected(UUID player);

    void handleLoginSuccess(UserConnection info);

    ViaPlatform<?> getPlatform();

    ViaProviders getProviders();

    boolean isDebug();

    void setDebug(boolean debug);

    ViaInjector getInjector();

    ViaVersionCommand getCommandHandler();

    ViaPlatformLoader getLoader();

    /**
     * Returns a mutable set of self-added subplatform version strings.
     * This set is expanded by the subplatform itself (e.g. ViaBackwards), and may not contain all running ones.
     *
     * @return mutable set of subplatform versions
     */
    Set<String> getSubPlatforms();

    /**
     * @see ViaConnectionManager#getConnectedClient(UUID)
     */
    @Nullable
    UserConnection getConnection(UUID playerUUID);

    /**
     * Adds a runnable to be executed when ViaVersion has finished its init before the full server load.
     *
     * @param runnable runnable to be executed
     */
    void addEnableListener(Runnable runnable);

    Protocol getBaseProtocol();

    boolean isBaseProtocol(Protocol protocol);
}
