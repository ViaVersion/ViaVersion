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
package com.viaversion.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.legacy.LegacyViaAPI;
import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.legacy.LegacyAPI;
import io.netty.buffer.ByteBuf;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ViaAPIBase<T> implements ViaAPI<T> {

    private final LegacyAPI<T> legacy = new LegacyAPI<>();

    @Override
    public ServerProtocolVersion getServerVersion() {
        return Via.getManager().getProtocolManager().getServerProtocolVersion();
    }

    @Override
    public ProtocolVersion getPlayerProtocolVersion(UUID uuid) {
        UserConnection connection = Via.getManager().getConnectionManager().getServerConnection(uuid);
        return connection != null ? connection.getProtocolInfo().protocolVersion() : ProtocolVersion.unknown;
    }

    @Override
    public String getVersion() {
        return Via.getPlatform().getPluginVersion();
    }

    @Override
    public boolean isInjected(UUID uuid) {
        return Via.getManager().getConnectionManager().hasServerConnection(uuid);
    }

    @Override
    public @Nullable UserConnection getConnection(final UUID uuid) {
        return Via.getManager().getConnectionManager().getServerConnection(uuid);
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException {
        if (!isInjected(uuid)) {
            throw new IllegalArgumentException("This player is not controlled by ViaVersion!");
        }

        UserConnection user = Via.getManager().getConnectionManager().getServerConnection(uuid);
        user.scheduleSendRawPacket(packet);
    }

    @Override
    public SortedSet<ProtocolVersion> getSupportedProtocolVersions() {
        SortedSet<ProtocolVersion> outputSet = new TreeSet<>(Via.getManager().getProtocolManager().getSupportedVersions());
        BlockedProtocolVersions blockedVersions = Via.getPlatform().getConf().blockedProtocolVersions();
        outputSet.removeIf(blockedVersions::contains);
        return outputSet;
    }

    @Override
    public SortedSet<ProtocolVersion> getFullSupportedProtocolVersions() {
        return Via.getManager().getProtocolManager().getSupportedVersions();
    }

    @Override
    public LegacyViaAPI<T> legacyAPI() {
        return legacy;
    }
}
