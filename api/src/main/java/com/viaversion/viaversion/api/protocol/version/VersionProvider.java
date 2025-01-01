/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.protocol.version;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;

@FunctionalInterface
public interface VersionProvider extends Provider {

    /**
     * Optionally allows platforms to specify the client version of a user. This is needed when the platform supports
     * connecting other version types then {@link VersionType#RELEASE} to the server.
     *
     * @param connection connection
     * @return client protocol version, or null if handshake packet should be used
     */
    default ProtocolVersion getClientProtocol(UserConnection connection) {
        return null;
    }

    /**
     * Calls {@link #getClosestServerProtocol(UserConnection)} and catches any exceptions by returning null.
     *
     * @param connection connection
     * @return closest server protocol version to the user's protocol version
     */
    default ProtocolVersion getServerProtocol(UserConnection connection) {
        try {
            return getClosestServerProtocol(connection);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the closest server protocol version to the user's protocol version.
     * On non-proxy servers, this returns the actual server version.
     *
     * @param connection connection
     * @return closest server protocol version to the user's protocol version
     */
    ProtocolVersion getClosestServerProtocol(UserConnection connection) throws Exception;
}
