/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.platform;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface ProtocolDetectorService {

    /**
     * Returns the protocol version of the proxied server, or -1 if unknown.
     *
     * @param serverName name of the proxied server
     * @return protocol version of the proxied server, or -1 if unknown
     */
    ProtocolVersion serverProtocolVersion(String serverName);

    /**
     * Probes all registered proxied servers for their protocol version.
     * This is executed automatically in the interval set in the ViaVersion config.
     */
    void probeAllServers();

    /**
     * Sets the stored protocol version of a proxied server.
     *
     * @param serverName      name of the proxied server
     * @param protocolVersion protocol version of the server
     */
    void setProtocolVersion(String serverName, int protocolVersion);

    /**
     * Uncaches and returns the previously stored protocol version of the proxied server. Returns -1 if none was stored.
     *
     * @param serverName name of the proxied server
     * @return previously stored protocol version of the proxied server, or -1 if none was present
     */
    int uncacheProtocolVersion(String serverName);

    /**
     * Returns an unmodifiable map of detected protocol versions.
     *
     * @return unmodifiable map of detected protocol versions
     */
    Object2IntMap<String> detectedProtocolVersions();
}
