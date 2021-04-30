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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.UUID;

public class ProtocolInfoImpl implements ProtocolInfo {
    private final UserConnection connection;
    private State state = State.HANDSHAKE;
    private int protocolVersion = -1;
    private int serverProtocolVersion = -1;
    private String username;
    private UUID uuid;
    private ProtocolPipeline pipeline;

    public ProtocolInfoImpl(UserConnection connection) {
        this.connection = connection;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public void setProtocolVersion(int protocolVersion) {
        // Map snapshot versions to the higher/orderer release version
        ProtocolVersion protocol = ProtocolVersion.getProtocol(protocolVersion);
        this.protocolVersion = protocol.getVersion();
    }

    @Override
    public int getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    @Override
    public void setServerProtocolVersion(int serverProtocolVersion) {
        ProtocolVersion protocol = ProtocolVersion.getProtocol(serverProtocolVersion);
        this.serverProtocolVersion = protocol.getVersion();
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
    public ProtocolPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void setPipeline(ProtocolPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public UserConnection getUser() {
        return connection;
    }

    @Override
    public String toString() {
        return "ProtocolInfo{" +
                "state=" + state +
                ", protocolVersion=" + protocolVersion +
                ", serverProtocolVersion=" + serverProtocolVersion +
                ", username='" + username + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
