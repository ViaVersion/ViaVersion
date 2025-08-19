/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.velocity.platform;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class VelocityViaConfig extends AbstractViaConfig {
    private int velocityPingInterval;
    private boolean velocityPingSave;
    private Map<String, Integer> velocityServerProtocols;

    public VelocityViaConfig(File folder, Logger logger) {
        super(new File(folder, "config.yml"), logger);
    }

    @Override
    protected void loadFields() {
        super.loadFields();
        velocityPingInterval = getInt("velocity-ping-interval", 60);
        velocityPingSave = getBoolean("velocity-ping-save", true);
        velocityServerProtocols = get("velocity-servers", new HashMap<>());
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
        // Parse servers
        Map<String, Object> servers;
        if (config.get("velocity-servers") instanceof Map velocityServers) {
            servers = velocityServers;
        } else {
            servers = new HashMap<>();
        }
        // Convert any bad Protocol Ids
        for (Map.Entry<String, Object> entry : new HashSet<>(servers.entrySet())) {
            if (!(entry.getValue() instanceof Integer)) {
                if (entry.getValue() instanceof String protocol) {
                    ProtocolVersion found = ProtocolVersion.getClosest(protocol);
                    if (found != null) {
                        servers.put(entry.getKey(), found.getVersion());
                    } else {
                        servers.remove(entry.getKey()); // Remove!
                    }
                } else {
                    servers.remove(entry.getKey()); // Remove!
                }
            }
        }
        // Ensure default exists
        if (!servers.containsKey("default")) {
            // Side note: This doesn't use ProtocolRegistry as it doesn't know the protocol version at boot.
            try {
                servers.put("default", VelocityViaInjector.getLowestSupportedProtocolVersion());
            } catch (Exception e) {
                // Something went very wrong
                e.printStackTrace();
            }
        }
        // Put back
        config.put("velocity-servers", servers);
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return BUKKIT_ONLY_OPTIONS;
    }

    @Override
    public Set<String> getSectionsWithModifiableKeys() {
        return Set.of("velocity-servers");
    }

    /**
     * What is the interval for checking servers via ping
     * -1 for disabled
     *
     * @return Ping interval in seconds
     */
    public int getVelocityPingInterval() {
        return velocityPingInterval;
    }

    /**
     * Should the velocity ping be saved to the config on change.
     *
     * @return True if it should save
     */
    public boolean isVelocityPingSave() {
        return velocityPingSave;
    }

    /**
     * Get the listed server protocols in the config.
     * default will be listed as default.
     *
     * @return Map of String, Integer
     */
    public Map<String, Integer> getVelocityServerProtocols() {
        return velocityServerProtocols;
    }
}
