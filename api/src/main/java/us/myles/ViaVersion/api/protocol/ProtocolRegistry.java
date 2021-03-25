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
package us.myles.ViaVersion.api.protocol;

import com.google.common.collect.Range;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.ViaManager;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * @see ViaManager#getProtocolManager()
 * @deprecated use {@link ProtocolManager}
 */
@Deprecated
public class ProtocolRegistry {
    @Deprecated
    public static int SERVER_PROTOCOL = -1;
    @Deprecated
    public static int maxProtocolPathSize = 50;

    /**
     * Register a protocol
     *
     * @param protocol  The protocol to register.
     * @param supported Supported client versions.
     * @param output    The output server version it converts to.
     */
    public static void registerProtocol(Protocol protocol, ProtocolVersion supported, ProtocolVersion output) {
        registerProtocol(protocol, Collections.singletonList(supported.getVersion()), output.getVersion());
    }

    /**
     * Register a protocol
     *
     * @param protocol  The protocol to register.
     * @param supported Supported client versions.
     * @param output    The output server version it converts to.
     */
    public static void registerProtocol(Protocol protocol, List<Integer> supported, int output) {
        Via.getManager().getProtocolManager().registerProtocol(protocol, supported, output);
    }

    /**
     * Registers a base protocol.
     * Base Protocols registered later have higher priority
     * Only one base protocol will be added to pipeline
     *
     * @param baseProtocol       Base Protocol to register
     * @param supportedProtocols Versions that baseProtocol supports
     */
    public static void registerBaseProtocol(Protocol baseProtocol, Range<Integer> supportedProtocols) {
        Via.getManager().getProtocolManager().registerBaseProtocol(baseProtocol, supportedProtocols);
    }

    /**
     * Get the versions compatible with the server.
     *
     * @return Read-only set of the versions.
     */
    public static SortedSet<Integer> getSupportedVersions() {
        return Via.getManager().getProtocolManager().getSupportedVersions();
    }

    /**
     * Check if this plugin is useful to the server.
     *
     * @return True if there is a useful pipe
     */
    public static boolean isWorkingPipe() {
        return Via.getManager().getProtocolManager().isWorkingPipe();
    }

    /**
     * Calculate a path from a client version to server version.
     *
     * @param clientVersion The input client version
     * @param serverVersion The desired output server version
     * @return The path it generated, null if it failed.
     */
    @Nullable
    public static List<Pair<Integer, Protocol>> getProtocolPath(int clientVersion, int serverVersion) {
        List<ProtocolPathEntry> pathList = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
        if (pathList == null) {
            return null;
        }

        List<Pair<Integer, Protocol>> list = new ArrayList<>();
        for (ProtocolPathEntry entry : pathList) {
            list.add(new Pair<>(entry.getOutputProtocolVersion(), entry.getProtocol()));
        }
        return list;
    }

    /**
     * Returns a protocol instance by its class.
     *
     * @param protocolClass class of the protocol
     * @return protocol if present
     */
    @Nullable
    public static Protocol getProtocol(Class<? extends Protocol> protocolClass) {
        return Via.getManager().getProtocolManager().getProtocol(protocolClass);
    }

    public static Protocol getBaseProtocol(int serverVersion) {
        return Via.getManager().getProtocolManager().getBaseProtocol(serverVersion);
    }

    public static boolean isBaseProtocol(Protocol protocol) {
        return Via.getManager().getProtocolManager().isBaseProtocol(protocol);
    }
}
