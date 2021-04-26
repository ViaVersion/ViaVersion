/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package us.myles.ViaVersion.protocols.base;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.packets.State;

import java.util.UUID;

public class ProtocolInfo extends StoredObject {
    private State state = State.HANDSHAKE;
    private int protocolVersion = -1;
    private int serverProtocolVersion = -1;
    private String username;
    private UUID uuid;
    private ProtocolPipeline pipeline;

    public ProtocolInfo(UserConnection user) {
        super(user);
    }

    /**
     * Returns the protocol state the user is currently in.
     *
     * @return protocol state
     */
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * Returns the user's protocol version, or -1 if not set.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return protocol version, or -1 if not set
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        // Map snapshot versions to the higher/orderer release version
        ProtocolVersion protocol = ProtocolVersion.getProtocol(protocolVersion);
        this.protocolVersion = protocol.getVersion();
    }

    /**
     * Returns the server protocol version the user is connected to, or -1 if not set.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return server protocol version, or -1 if not set
     */
    public int getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public void setServerProtocolVersion(int serverProtocolVersion) {
        ProtocolVersion protocol = ProtocolVersion.getProtocol(serverProtocolVersion);
        this.serverProtocolVersion = protocol.getVersion();
    }

    /**
     * Returns the username associated with this connection.
     * This is set once the connection enters the {@link State#PLAY} state.
     *
     * @return username, set when entering the {@link State#PLAY} state
     */
    public @MonotonicNonNull String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the uuid associated with this connection.
     * This is set once the connection enters the {@link State#PLAY} state.
     *
     * @return uuid, set when entering the {@link State#PLAY} state
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the user's pipeline.
     *
     * @return protocol pipeline
     */
    public ProtocolPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(ProtocolPipeline pipeline) {
        this.pipeline = pipeline;
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
