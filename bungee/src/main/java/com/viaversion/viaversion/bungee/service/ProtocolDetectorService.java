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
package com.viaversion.viaversion.bungee.service;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bungee.platform.BungeeViaConfig;
import com.viaversion.viaversion.bungee.providers.BungeeVersionProvider;
import com.viaversion.viaversion.platform.AbstractProtocolDetectorService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public final class ProtocolDetectorService extends AbstractProtocolDetectorService {

    public void probeServer(final ServerInfo serverInfo) {
        final String serverName = serverInfo.getName();
        serverInfo.ping((serverPing, throwable) -> {
            // Ensure protocol is positive, some services will return -1
            if (throwable != null || serverPing == null || serverPing.getVersion() == null || serverPing.getVersion().getProtocol() <= 0) {
                return;
            }

            final int oldProtocolVersion = serverProtocolVersion(serverName).getVersion();
            if (oldProtocolVersion == serverPing.getVersion().getProtocol()) {
                // Same value as previously
                return;
            }

            setProtocolVersion(serverName, serverPing.getVersion().getProtocol());

            final BungeeViaConfig config = (BungeeViaConfig) Via.getConfig();
            if (config.isBungeePingSave()) {
                final Map<String, Integer> servers = config.getBungeeServerProtocols();
                final Integer protocol = servers.get(serverName);
                if (protocol != null && protocol == serverPing.getVersion().getProtocol()) {
                    return;
                }

                // Ensure we're the only ones writing to the config
                synchronized (Via.getPlatform().getConfigurationProvider()) {
                    servers.put(serverName, serverPing.getVersion().getProtocol());
                }
                config.save();
            }
        });
    }

    @Override
    public void probeAllServers() {
        final Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
        final Set<String> serverNames = new HashSet<>(servers.size());
        for (final ServerInfo serverInfo : servers) {
            probeServer(serverInfo);
            serverNames.add(serverInfo.getName());
        }

        // Remove servers that aren't registered anymore
        lock.writeLock().lock();
        try {
            detectedProtocolIds.keySet().retainAll(serverNames);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected Map<String, Integer> configuredServers() {
        return ((BungeeViaConfig) Via.getConfig()).getBungeeServerProtocols();
    }

    @Override
    protected ProtocolVersion lowestSupportedProtocolVersion() {
        return BungeeVersionProvider.getLowestSupportedVersion();
    }
}
