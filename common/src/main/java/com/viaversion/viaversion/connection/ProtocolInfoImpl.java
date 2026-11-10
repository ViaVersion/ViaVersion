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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.UUID;

public class ProtocolInfoImpl implements ProtocolInfo {
    private State clientState = State.HANDSHAKE;
    private State serverState = State.HANDSHAKE;
    private ProtocolVersion serverProtocolVersion = ProtocolVersion.unknown;
    private ProtocolVersion protocolVersion = ProtocolVersion.unknown;
    private String username;
    private UUID uuid;
    private ProtocolPipeline pipeline;
    private boolean compressionEnabled;

    @Override
    public State getClientState() {
        return clientState;
    }

    @Override
    public void setClientState(final State clientState) {
        if (Via.getManager().debugHandler().enabled()) {
            Via.getPlatform().getLogger().info("Client state changed from " + this.clientState + " to " + clientState + " for " + uuid);
        }
        this.clientState = clientState;
    }

    @Override
    public State getServerState() {
        return serverState;
    }

    @Override
    public void setServerState(final State serverState) {
        if (Via.getManager().debugHandler().enabled()) {
            Via.getPlatform().getLogger().info("Server state changed from " + this.serverState + " to " + serverState + " for " + uuid);
        }
        this.serverState = serverState;
    }

    @Override
    public ProtocolVersion protocolVersion() {
        return protocolVersion;
    }

    @Override
    public void setProtocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public ProtocolVersion serverProtocolVersion() {
        return serverProtocolVersion;
    }

    @Override
    public void setServerProtocolVersion(ProtocolVersion serverProtocolVersion) {
        this.serverProtocolVersion = serverProtocolVersion;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean compressionEnabled() {
        return compressionEnabled;
    }

    @Override
    public void setCompressionEnabled(final boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    @Override
    public ProtocolPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void setPipeline(ProtocolPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String toString() {
        return "ProtocolInfo{" +
            "clientState=" + clientState +
            ", serverState=" + serverState +
            ", protocolVersion=" + protocolVersion +
            ", serverProtocolVersion=" + serverProtocolVersion +
            ", username='" + username + '\'' +
            ", uuid=" + uuid +
            '}';
    }
}
