/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ProtocolInfo {

    /**
     * Returns the protocol state the client is currently in.
     *
     * @return the client protocol state
     */
    State getClientState();

    /**
     * Returns the protocol state the server is currently in.
     *
     * @return the server protocol state
     */
    State getServerState();

    /**
     * Returns the protocol state for the given direction.
     *
     * @param direction protocol direction
     * @return state for the given direction
     */
    default State getState(final Direction direction) {
        // Return the state the packet is coming from
        return direction == Direction.CLIENTBOUND ? this.getServerState() : this.getClientState();
    }

    /**
     * Sets both client and server state.
     *
     * @param state the new protocol state
     * @see #setClientState(State)
     * @see #setServerState(State)
     */
    default void setState(final State state) {
        this.setClientState(state);
        this.setServerState(state);
    }

    /**
     * Sets the client protocol state.
     *
     * @param clientState the new client protocol state
     */
    void setClientState(State clientState);

    /**
     * Sets the server protocol state.
     *
     * @param serverState the new server protocol state
     */
    void setServerState(State serverState);

    /**
     * Returns the user's protocol version, or null if not set.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return protocol version, may be unknown
     * @see ProtocolVersion#isKnown()
     */
    ProtocolVersion protocolVersion();

    void setProtocolVersion(ProtocolVersion protocolVersion);

    /**
     * Returns the server protocol version the user is connected to.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return the server protocol version the user is connected to, may be unknown
     * @see ProtocolVersion#isKnown()
     */
    ProtocolVersion serverProtocolVersion();

    void setServerProtocolVersion(ProtocolVersion protocolVersion);

    @Deprecated
    default int getProtocolVersion() {
        return protocolVersion() != null ? protocolVersion().getVersion() : -1;
    }

    @Deprecated
    default int getServerProtocolVersion() {
        return serverProtocolVersion() != null ? serverProtocolVersion().getVersion() : -1;
    }

    /**
     * Returns the username associated with this connection.
     * This is set once the connection enters the {@link State#PLAY} state.
     *
     * @return username, set when entering the {@link State#PLAY} state
     */
    @Nullable String getUsername();

    void setUsername(String username);

    /**
     * Returns the uuid associated with this connection.
     * This is set once the connection enters the {@link State#PLAY} state.
     *
     * @return uuid, set when entering the {@link State#PLAY} state
     */
    @Nullable UUID getUuid();

    void setUuid(UUID uuid);

    /**
     * Returns whether compression is enabled for this connection.
     *
     * @return whether compression is enabled
     */
    boolean compressionEnabled();

    void setCompressionEnabled(boolean compressionEnabled);

    /**
     * Returns the user's pipeline.
     *
     * @return protocol pipeline
     */
    ProtocolPipeline getPipeline();

    void setPipeline(ProtocolPipeline pipeline);
}
