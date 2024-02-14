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

import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Set;

public class BlockedProtocolVersionsImpl implements BlockedProtocolVersions {
    private final Set<ProtocolVersion> singleBlockedVersions;
    private final ProtocolVersion blocksBelow;
    private final ProtocolVersion blocksAbove;

    public BlockedProtocolVersionsImpl(final Set<ProtocolVersion> singleBlockedVersions, final ProtocolVersion blocksBelow, final ProtocolVersion blocksAbove) {
        this.singleBlockedVersions = singleBlockedVersions;
        this.blocksBelow = blocksBelow;
        this.blocksAbove = blocksAbove;
    }

    @Override
    public boolean contains(final ProtocolVersion protocolVersion) {
        return blocksBelow.isKnown() && protocolVersion.olderThan(blocksBelow)
                || blocksAbove.isKnown() && protocolVersion.newerThan(blocksAbove)
                || singleBlockedVersions.contains(protocolVersion);
    }

    @Override
    public ProtocolVersion blocksBelow() {
        return blocksBelow;
    }

    @Override
    public ProtocolVersion blocksAbove() {
        return blocksAbove;
    }

    @Override
    public Set<ProtocolVersion> singleBlockedVersions() {
        return singleBlockedVersions;
    }
}
