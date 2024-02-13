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
package com.viaversion.viaversion.protocol;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import java.util.SortedSet;

public class ServerProtocolVersionRange implements ServerProtocolVersion {
    private final ProtocolVersion lowestSupportedVersion;
    private final ProtocolVersion highestSupportedVersion;
    private final SortedSet<ProtocolVersion> supportedVersions;

    public ServerProtocolVersionRange(ProtocolVersion lowestSupportedVersion, ProtocolVersion highestSupportedVersion, SortedSet<ProtocolVersion> supportedVersions) {
        this.lowestSupportedVersion = lowestSupportedVersion;
        this.highestSupportedVersion = highestSupportedVersion;
        this.supportedVersions = supportedVersions;
    }

    @Override
    public ProtocolVersion lowestSupportedProtocolVersion() {
        return lowestSupportedVersion;
    }

    @Override
    public ProtocolVersion highestSupportedProtocolVersion() {
        return highestSupportedVersion;
    }

    @Override
    public SortedSet<ProtocolVersion> supportedProtocolVersions() {
        return supportedVersions;
    }
}
