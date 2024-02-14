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

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.Objects;

public class ProtocolPathEntryImpl implements ProtocolPathEntry {
    private final ProtocolVersion outputProtocolVersion;
    private final Protocol<?, ?, ?, ?> protocol;

    public ProtocolPathEntryImpl(ProtocolVersion outputProtocolVersion, Protocol<?, ?, ?, ?> protocol) {
        this.outputProtocolVersion = outputProtocolVersion;
        this.protocol = protocol;
    }

    @Override
    public ProtocolVersion outputProtocolVersion() {
        return outputProtocolVersion;
    }

    @Override
    public Protocol<?, ?, ?, ?> protocol() {
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
        return Objects.hash(outputProtocolVersion, protocol);
    }

    @Override
    public String toString() {
        return "ProtocolPathEntryImpl{" +
                "outputProtocolVersion=" + outputProtocolVersion +
                ", protocol=" + protocol +
                '}';
    }
}