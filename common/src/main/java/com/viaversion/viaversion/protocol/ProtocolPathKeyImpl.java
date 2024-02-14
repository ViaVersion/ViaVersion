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

import com.viaversion.viaversion.api.protocol.ProtocolPathKey;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Objects;

public class ProtocolPathKeyImpl implements ProtocolPathKey {
    private final ProtocolVersion clientProtocolVersion;
    private final ProtocolVersion serverProtocolVersion;

    public ProtocolPathKeyImpl(ProtocolVersion clientProtocolVersion, ProtocolVersion serverProtocolVersion) {
        this.clientProtocolVersion = clientProtocolVersion;
        this.serverProtocolVersion = serverProtocolVersion;
    }

    @Override
    public ProtocolVersion clientProtocolVersion() {
        return clientProtocolVersion;
    }

    @Override
    public ProtocolVersion serverProtocolVersion() {
        return serverProtocolVersion;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ProtocolPathKeyImpl that = (ProtocolPathKeyImpl) o;
        if (clientProtocolVersion != that.clientProtocolVersion) return false;
        return serverProtocolVersion == that.serverProtocolVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientProtocolVersion, serverProtocolVersion);
    }
}
