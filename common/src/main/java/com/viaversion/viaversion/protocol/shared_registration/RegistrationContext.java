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

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public record RegistrationContext<CU extends ClientboundPacketType, SU extends ServerboundPacketType>(
    AbstractProtocol<CU, ?, ?, SU> protocol,
    ProtocolVersion min,
    @Nullable ProtocolVersion max
) {

    public <P extends AbstractProtocol<CU, ?, ?, SU>> P protocol(@SuppressWarnings("unused") final Class<P> protocolClass) {
        //noinspection unchecked
        return (P) protocol;
    }

    public <P extends AbstractProtocol<CU, ?, ?, SU>> P castProtocol() {
        //noinspection unchecked
        return (P) protocol;
    }

    public void clientbound(final ClientboundPacketType type, final Consumer<CU> action) {
        action.accept(clientboundPacketType(type));
    }

    public void clientbound(final ClientboundPacketType type, final Consumer<CU> action, final PacketBound... markers) {
        if (!shouldSkip(markers)) {
            action.accept(clientboundPacketType(type));
        }
    }

    public void clientboundHandler(final ClientboundPacketType type, final PacketHandler handler) {
        protocol.registerClientbound(clientboundPacketType(type), handler);
    }

    public void clientboundHandler(final ClientboundPacketType type, final PacketHandler handler, final PacketBound... markers) {
        if (!shouldSkip(markers)) {
            protocol.registerClientbound(clientboundPacketType(type), handler);
        }
    }

    public void serverbound(final ServerboundPacketType type, final Consumer<SU> action) {
        action.accept(serverboundPacketType(type));
    }

    public void serverbound(final ServerboundPacketType type, final Consumer<SU> action, final PacketBound... markers) {
        if (!shouldSkip(markers)) {
            action.accept(serverboundPacketType(type));
        }
    }

    public void serverboundHandler(final ServerboundPacketType type, final PacketHandler handler) {
        protocol.registerServerbound(serverboundPacketType(type), handler);
    }

    public void serverboundHandler(final ServerboundPacketType type, final PacketHandler handler, final PacketBound... markers) {
        if (!shouldSkip(markers)) {
            protocol.registerServerbound(serverboundPacketType(type), handler);
        }
    }

    private boolean shouldSkip(final PacketBound... markers) {
        if (markers.length == 0) {
            return false;
        }

        final ProtocolVersion serverVersion = protocol.getServerVersion();
        final ProtocolVersion clientVersion = protocol.getClientVersion();
        final ProtocolVersion lowerVersion = serverVersion.olderThan(clientVersion) ? serverVersion : clientVersion;
        final ProtocolVersion higherVersion = serverVersion.newerThan(clientVersion) ? serverVersion : clientVersion;
        for (final PacketBound marker : markers) {
            switch (marker) {
                case ADDED_AT_MIN -> {
                    if (lowerVersion.olderThan(min)) {
                        return true;
                    }
                }
                case REMOVED_AT_MAX -> {
                    Preconditions.checkArgument(max != null, "Cannot use REMOVED_AT_MAX in an open-ended range");
                    if (higherVersion.newerThanOrEqualTo(max)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private SU serverboundPacketType(final ServerboundPacketType genericPacketType) {
        final SU type = protocol.getPacketTypesProvider().unmappedServerboundType(genericPacketType.state(), genericPacketType.getName());
        if (type == null) {
            throw new IllegalArgumentException("Could not find serverbound packet type for " + genericPacketType.state() + " " + genericPacketType.getName());
        }
        return type;
    }

    private CU clientboundPacketType(final ClientboundPacketType genericPacketType) {
        final CU type = protocol.getPacketTypesProvider().unmappedClientboundType(genericPacketType.state(), genericPacketType.getName());
        if (type == null) {
            throw new IllegalArgumentException("Could not find clientbound packet type for " + genericPacketType.state() + " " + genericPacketType.getName() + " in " + protocol.getClass().getSimpleName());
        }
        return type;
    }
}
