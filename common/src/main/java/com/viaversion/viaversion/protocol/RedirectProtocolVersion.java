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
import com.viaversion.viaversion.api.protocol.version.SubVersionRange;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import java.util.Comparator;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Intended API class for protocol versions with the version type {@link VersionType#SPECIAL}.
 * <p>
 * Compares equal to the given origin version and allows base protocol determination via {@link #getBaseProtocolVersion()}
 * which can be null for special cases where there is no base protocol.
 */
public class RedirectProtocolVersion extends ProtocolVersion {

    private final ProtocolVersion origin;

    public RedirectProtocolVersion(final int version, final String name, final ProtocolVersion origin) {
        this(version, -1, name, null, origin);
    }

    /**
     * See {@link ProtocolVersion} for more information.
     */
    public RedirectProtocolVersion(final int version, final int snapshotVersion, final String name, @Nullable final SubVersionRange versionRange, final ProtocolVersion origin) {
        super(VersionType.SPECIAL, version, snapshotVersion, name, versionRange);
        this.origin = origin;
    }

    @Override
    protected @Nullable Comparator<ProtocolVersion> customComparator() {
        return (o1, o2) -> {
            if (o1 == this) o1 = this.origin;
            if (o2 == this) o2 = this.origin;
            return o1.compareTo(o2);
        };
    }

    public ProtocolVersion getOrigin() {
        return origin;
    }

    /**
     * @return the protocol version used to determine the base protocol, null in case there is no base protocol.
     */
    public @Nullable ProtocolVersion getBaseProtocolVersion() {
        return origin;
    }
}
