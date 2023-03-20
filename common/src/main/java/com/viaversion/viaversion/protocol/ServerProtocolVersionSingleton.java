/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;

public class ServerProtocolVersionSingleton implements ServerProtocolVersion {
    private final int protocolVersion;

    public ServerProtocolVersionSingleton(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public int lowestSupportedVersion() {
        return protocolVersion;
    }

    @Override
    public int highestSupportedVersion() {
        return protocolVersion;
    }

    @Override
    public IntSortedSet supportedVersions() {
        return IntSortedSets.singleton(protocolVersion);
    }
}
