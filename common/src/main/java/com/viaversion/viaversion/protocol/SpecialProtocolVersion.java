/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
public class SpecialProtocolVersion extends ProtocolVersion {

    private final ProtocolVersion delegate;

    public SpecialProtocolVersion(final int version, final String name, final ProtocolVersion delegate) {
        this(version, -1, name, null, delegate);
    }

    /**
     * See {@link ProtocolVersion} for more information.
     */
    public SpecialProtocolVersion(final int version, final int snapshotVersion, final String name, @Nullable final SubVersionRange versionRange, final ProtocolVersion delegate) {
        super(VersionType.SPECIAL, version, snapshotVersion, name, versionRange);
        this.delegate = delegate;
    }

    @Override
    protected @Nullable Comparator<ProtocolVersion> customComparator() {
        return (o1, o2) -> {
            if (o1 == this) o1 = this.delegate;
            if (o2 == this) o2 = this.delegate;
            return o1.compareTo(o2);
        };
    }

    public ProtocolVersion getDelegate() {
        return delegate;
    }

    /**
     * @return the protocol version used to determine the base protocol, null in case there is no base protocol.
     */
    public @Nullable ProtocolVersion getBaseProtocolVersion() {
        return delegate;
    }
}
