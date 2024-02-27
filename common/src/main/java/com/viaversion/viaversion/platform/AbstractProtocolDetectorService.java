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
package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.platform.ProtocolDetectorService;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractProtocolDetectorService implements ProtocolDetectorService {
    protected final Object2IntMap<String> detectedProtocolIds = new Object2IntOpenHashMap<>();
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected AbstractProtocolDetectorService() {
        detectedProtocolIds.defaultReturnValue(-1);
    }

    @Override
    public ProtocolVersion serverProtocolVersion(final String serverName) {
        // Step 1. Check detected
        lock.readLock().lock();
        final int detectedProtocol;
        try {
            detectedProtocol = detectedProtocolIds.getInt(serverName);
        } finally {
            lock.readLock().unlock();
        }
        if (detectedProtocol != -1) {
            return ProtocolVersion.getProtocol(detectedProtocol);
        }

        // Step 2. Check config (CME moment?)
        final Map<String, Integer> servers = configuredServers();
        final Integer protocol = servers.get(serverName);
        if (protocol != null) {
            return ProtocolVersion.getProtocol(protocol);
        }

        // Step 3. Use Default (CME moment intensifies?)
        final Integer defaultProtocol = servers.get("default");
        if (defaultProtocol != null) {
            return ProtocolVersion.getProtocol(defaultProtocol);
        }

        // Step 4: Use the proxy's lowest supported... *cries*
        return lowestSupportedProtocolVersion();
    }

    @Override
    public void setProtocolVersion(final String serverName, final int protocolVersion) {
        lock.writeLock().lock();
        try {
            detectedProtocolIds.put(serverName, protocolVersion);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int uncacheProtocolVersion(final String serverName) {
        lock.writeLock().lock();
        try {
            return detectedProtocolIds.removeInt(serverName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Object2IntMap<String> detectedProtocolVersions() {
        lock.readLock().lock();
        try {
            return new Object2IntOpenHashMap<>(detectedProtocolIds);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected abstract Map<String, Integer> configuredServers();

    protected abstract ProtocolVersion lowestSupportedProtocolVersion();
}
