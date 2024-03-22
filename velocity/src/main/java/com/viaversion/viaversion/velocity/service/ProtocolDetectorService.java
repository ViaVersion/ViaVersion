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
package com.viaversion.viaversion.velocity.service;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.platform.AbstractProtocolDetectorService;
import com.viaversion.viaversion.velocity.platform.VelocityViaConfig;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class ProtocolDetectorService extends AbstractProtocolDetectorService {

    @Override
    public void probeAllServers() {
        final Collection<RegisteredServer> servers = VelocityPlugin.PROXY.getAllServers();
        final Set<String> serverNames = new HashSet<>(servers.size());
        for (final RegisteredServer server : servers) {
            probeServer(server);
            serverNames.add(server.getServerInfo().getName());
        }

        // Remove servers that aren't registered anymore
        lock.writeLock().lock();
        try {
            detectedProtocolIds.keySet().retainAll(serverNames);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void probeServer(final RegisteredServer server) {
        final String serverName = server.getServerInfo().getName();
        server.ping().thenAccept(serverPing -> {
            if (serverPing == null || serverPing.getVersion() == null) {
                return;
            }

            final ProtocolVersion oldProtocolVersion = serverProtocolVersion(serverName);
            if (oldProtocolVersion.isKnown() && oldProtocolVersion.getVersion() == serverPing.getVersion().getProtocol()) {
                // Same value as previously
                return;
            }

            setProtocolVersion(serverName, serverPing.getVersion().getProtocol());

            final VelocityViaConfig config = (VelocityViaConfig) Via.getConfig();
            if (config.isVelocityPingSave()) {
                final Map<String, Integer> servers = configuredServers();
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
    protected Map<String, Integer> configuredServers() {
        return ((VelocityViaConfig) Via.getConfig()).getVelocityServerProtocols();
    }

    @Override
    protected ProtocolVersion lowestSupportedProtocolVersion() {
        try {
            return Via.getManager().getInjector().getServerProtocolVersion();
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to get lowest supported protocol version", e);
            return ProtocolVersion.v1_8;
        }
    }
}
