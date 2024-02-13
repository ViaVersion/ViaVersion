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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.SortedSet;

public class ServerProtocolVersionSingleton implements ServerProtocolVersion {
    private final ProtocolVersion protocolVersion;

    public ServerProtocolVersionSingleton(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public ProtocolVersion lowestSupportedProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public ProtocolVersion highestSupportedProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public SortedSet<ProtocolVersion> supportedProtocolVersions() {
        final SortedSet<ProtocolVersion> set = new ObjectLinkedOpenHashSet<>();
        set.add(protocolVersion);
        return set;
    }
}
