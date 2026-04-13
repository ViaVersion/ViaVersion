/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocol.shared_registration;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Central registry for shared packet registrations that are automatically applied
 * to protocols based on their version range.
 * <p>
 * Registrations are done BEFORE registerPackets in {@link AbstractProtocol}.
 */
public final class SharedRegistrations {
    private static final SharedRegistrations DEFAULT_REGISTRATIONS = new SharedRegistrations();

    private final EnumMap<VersionType, List<VersionedTemplateGroup>> versionedTemplates = new EnumMap<>(VersionType.class);

    /**
     * Returns the default ViaVersion registration set.
     */
    public static SharedRegistrations defaultRegistrations() {
        return DEFAULT_REGISTRATIONS;
    }

    /**
     * Creates an independent shared registration set that external projects can populate and apply.
     */
    public static SharedRegistrations create() {
        return new SharedRegistrations();
    }

    public RegistrationBuilder registrations() {
        return new RegistrationBuilder(this);
    }

    /**
     * Applies all shared registrations whose version range matches the given protocol.
     *
     * @param protocol the protocol to apply shared registrations to
     */
    public void applyMatching(final AbstractProtocol<?, ?, ?, ?> protocol) {
        if (protocol.isBaseProtocol()) {
            return;
        }

        final ProtocolVersion version = protocol.getServerVersion();
        if (version.getVersionType() != protocol.getClientVersion().getVersionType()) {
            // Built on continuous version ranges
            return;
        }

        final List<VersionedTemplateGroup> groups = versionedTemplates.get(version.getVersionType());
        if (groups == null) {
            return;
        }

        for (final VersionedTemplateGroup group : groups) {
            if (group.min().newerThan(version)) { // TODO binary search?
                // Sorted by the minimum, so we can stop early if we're out
                break;
            }

            if (group.max() != null && version.newerThanOrEqualTo(group.max())) {
                continue;
            }

            final RegistrationContext<?, ?> context = new RegistrationContext<>(protocol, group.min(), group.max());
            //noinspection unchecked,rawtypes
            ((RegistrationAction) group.action()).accept(context);
        }
    }

    @FunctionalInterface
    public interface RegistrationAction<CU extends ClientboundPacketType, SU extends ServerboundPacketType> {

        void accept(RegistrationContext<CU, SU> context);
    }

    void register(final VersionType versionType, final List<VersionedTemplateGroup> toAdd) {
        final List<VersionedTemplateGroup> groups = versionedTemplates.computeIfAbsent(versionType, $ -> new ArrayList<>());
        groups.addAll(toAdd);
        groups.sort(Comparator.comparing(VersionedTemplateGroup::min, Comparator.nullsFirst(Comparator.naturalOrder())));
    }
}
