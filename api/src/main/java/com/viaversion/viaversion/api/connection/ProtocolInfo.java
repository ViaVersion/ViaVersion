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
package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.State;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface ProtocolInfo {

    /**
     * Returns the protocol state the user is currently in.
     *
     * @return protocol state
     */
    State getState();

    void setState(State state);

    /**
     * Returns the user's protocol version, or -1 if not set.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return protocol version, or -1 if not set
     */
    int getProtocolVersion();

    void setProtocolVersion(int protocolVersion);

    /**
     * Returns the server protocol version the user is connected to, or -1 if not set.
     * This is set during the {@link State#HANDSHAKE} state.
     *
     * @return server protocol version, or -1 if not set
     */
    int getServerProtocolVersion();

    void setServerProtocolVersion(int serverProtocolVersion);

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
     * Returns the user's pipeline.
     *
     * @return protocol pipeline
     */
    ProtocolPipeline getPipeline();

    void setPipeline(ProtocolPipeline pipeline);

    /**
     * Returns the user connection this info represents.
     *
     * @return user connection
     */
    UserConnection getUser();
}
