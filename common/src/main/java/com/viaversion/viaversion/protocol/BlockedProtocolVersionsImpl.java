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

import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;
import it.unimi.dsi.fastutil.ints.IntSet;

public class BlockedProtocolVersionsImpl implements BlockedProtocolVersions {
    private final IntSet singleBlockedVersions;
    private final int blocksBelow;
    private final int blocksAbove;

    public BlockedProtocolVersionsImpl(final IntSet singleBlockedVersions, final int blocksBelow, final int blocksAbove) {
        this.singleBlockedVersions = singleBlockedVersions;
        this.blocksBelow = blocksBelow;
        this.blocksAbove = blocksAbove;
    }

    @Override
    public boolean contains(final int protocolVersion) {
        return blocksBelow != -1 && protocolVersion < blocksBelow
                || blocksAbove != -1 && protocolVersion > blocksAbove
                || singleBlockedVersions.contains(protocolVersion);
    }

    @Override
    public int blocksBelow() {
        return blocksBelow;
    }

    @Override
    public int blocksAbove() {
        return blocksAbove;
    }

    @Override
    public IntSet singleBlockedVersions() {
        return singleBlockedVersions;
    }
}
