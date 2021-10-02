/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.viaversion.viaversion.BungeePlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bungee.platform.BungeeViaConfig;
import com.viaversion.viaversion.bungee.providers.BungeeVersionProvider;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolDetectorService implements Runnable {
    private static final Map<String, Integer> detectedProtocolIds = new ConcurrentHashMap<>();
    private static ProtocolDetectorService instance;
    private final BungeePlugin plugin;

    public ProtocolDetectorService(BungeePlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static Integer getProtocolId(String serverName) {
        // Step 1. Check Config
        Map<String, Integer> servers = ((BungeeViaConfig) Via.getConfig()).getBungeeServerProtocols();
        Integer protocol = servers.get(serverName);
        if (protocol != null) {
            return protocol;
        }
        // Step 2. Check Detected
        Integer detectedProtocol = detectedProtocolIds.get(serverName);
        if (detectedProtocol != null) {
            return detectedProtocol;
        }
        // Step 3. Use Default
        Integer defaultProtocol = servers.get("default");
        if (defaultProtocol != null) {
            return defaultProtocol;
        }
        // Step 4: Use bungee lowest supported... *cries*
        return BungeeVersionProvider.getLowestSupportedVersion();
    }

    @Override
    public void run() {
        for (final Map.Entry<String, ServerInfo> lists : plugin.getProxy().getServers().entrySet()) {
            probeServer(lists.getValue());
        }
    }

    public static void probeServer(final ServerInfo serverInfo) {
        final String key = serverInfo.getName();
        serverInfo.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing serverPing, Throwable throwable) {
                if (throwable == null && serverPing != null && serverPing.getVersion() != null) {
                    // Ensure protocol is positive, some services will return -1
                    if (serverPing.getVersion().getProtocol() > 0) {
                        detectedProtocolIds.put(key, serverPing.getVersion().getProtocol());
                        if (((BungeeViaConfig) Via.getConfig()).isBungeePingSave()) {
                            Map<String, Integer> servers = ((BungeeViaConfig) Via.getConfig()).getBungeeServerProtocols();
                            Integer protocol = servers.get(key);
                            if (protocol != null && protocol == serverPing.getVersion().getProtocol()) {
                                return;
                            }
                            // Ensure we're the only ones writing to the config
                            synchronized (Via.getPlatform().getConfigurationProvider()) {
                                servers.put(key, serverPing.getVersion().getProtocol());
                            }
                            // Save
                            Via.getPlatform().getConfigurationProvider().saveConfig();
                        }
                    }
                }
            }
        });
    }

    public static Map<String, Integer> getDetectedIds() {
        return new HashMap<>(detectedProtocolIds);
    }

    public static ProtocolDetectorService getInstance() {
        return instance;
    }

    public BungeePlugin getPlugin() {
        return plugin;
    }
}
