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
package com.viaversion.viaversion.protocol;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;

public class ProtocolPathEntryImpl implements ProtocolPathEntry {
    private final int outputProtocolVersion;
    private final Protocol protocol;

    public ProtocolPathEntryImpl(int outputProtocolVersion, Protocol protocol) {
        this.outputProtocolVersion = outputProtocolVersion;
        this.protocol = protocol;
    }

    @Override
    public int getOutputProtocolVersion() {
        return outputProtocolVersion;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ProtocolPathEntryImpl that = (ProtocolPathEntryImpl) o;
        if (outputProtocolVersion != that.outputProtocolVersion) return false;
        return protocol.equals(that.protocol);
    }

    @Override
    public int hashCode() {
        int result = outputProtocolVersion;
        result = 31 * result + protocol.hashCode();
        return result;
    }
}