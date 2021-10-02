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
package com.viaversion.viaversion.velocity.service;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.velocity.platform.VelocityViaConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolDetectorService implements Runnable {
    private static final Map<String, Integer> detectedProtocolIds = new ConcurrentHashMap<>();
    private static ProtocolDetectorService instance;

    public ProtocolDetectorService() {
        instance = this;
    }

    public static Integer getProtocolId(String serverName) {
        // Step 1. Check Config
        Map<String, Integer> servers = ((VelocityViaConfig) Via.getConfig()).getVelocityServerProtocols();
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
        try {
            return ProtocolVersion.getProtocol(Via.getManager().getInjector().getServerProtocolVersion()).getVersion();
        } catch (Exception e) {
            e.printStackTrace();
            return ProtocolVersion.v1_8.getVersion();
        }
    }

    @Override
    public void run() {
        for (final RegisteredServer serv : VelocityPlugin.PROXY.getAllServers()) {
            probeServer(serv);
        }
    }

    public static void probeServer(final RegisteredServer serverInfo) {
        final String key = serverInfo.getServerInfo().getName();
        serverInfo.ping().thenAccept((serverPing) -> {
            if (serverPing != null && serverPing.getVersion() != null) {
                detectedProtocolIds.put(key, serverPing.getVersion().getProtocol());
                if (((VelocityViaConfig) Via.getConfig()).isVelocityPingSave()) {
                    Map<String, Integer> servers = ((VelocityViaConfig) Via.getConfig()).getVelocityServerProtocols();
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
        });
    }

    public static Map<String, Integer> getDetectedIds() {
        return new HashMap<>(detectedProtocolIds);
    }

    public static ProtocolDetectorService getInstance() {
        return instance;
    }
}
