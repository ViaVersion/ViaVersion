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

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.nio.charset.StandardCharsets;

/**
 * Network protocol to provide information to backend and frontend servers about the
 * connected players and the server. See the documentation for more information:
 * <p>
 * <a href="https://github.com/ViaVersion/ViaVersion/wiki/Server-&-Player-Details-Protocol">Server/Player Details Protocol</a>
 */
public final class ConnectionDetails {

    public static final String PROXY_CHANNEL = "vv:proxy_details"; // Used for multi server proxies like Velocity
    public static final String SERVER_CHANNEL = "vv:server_details"; // Used for backend servers like Paper
    public static final String MOD_CHANNEL = "vv:mod_details"; // Used for clientside mods like ViaFabric
    public static final String APP_CHANNEL = "vv:app_details"; // Used for standalone applications

    private static final int VERSION = 1;

    /**
     * Sends both player and server details to the proxy fronting the server.
     *
     * @param connection the user connection
     * @param channel    the channel to send the details to
     */
    public static void sendConnectionDetails(final UserConnection connection, final String channel) {
        sendPlayerDetails(connection, channel);
        sendServerDetails(connection, channel);
    }

    /**
     * Sends the running ViaVersion version and native version of a player to the proxy fronting the server.
     * <p>
     * Requires {@link ViaPlatform#sendCustomPayload(UserConnection, String, byte[])} to be implemented
     *
     * @param connection the user connection
     * @param channel    the channel to send the details to
     */
    public static void sendPlayerDetails(final UserConnection connection, final String channel) {
        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        final ProtocolVersion nativeVersion = protocolInfo.protocolVersion();
        final ProtocolVersion serverVersion = protocolInfo.serverProtocolVersion();
        if (serverVersion.equals(nativeVersion)) {
            // No need to send details if the native version is the same as the server version
            return;
        }

        final String platformName = Via.getPlatform().getPlatformName();
        final String platformVersion = Via.getPlatform().getPlatformVersion();

        final JsonObject payload = new JsonObject();
        payload.addProperty("specVersion", VERSION);

        payload.addProperty("platformName", platformName);
        payload.addProperty("platformVersion", platformVersion);
        payload.addProperty("version", nativeVersion.getOriginalVersion());
        payload.addProperty("versionName", nativeVersion.getName());

        Via.getPlatform().sendCustomPayload(connection, channel, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends the server's version details to the connected client.
     * <p>
     * Requires {@link ViaPlatform#sendCustomPayloadToClient(UserConnection, String, byte[])} to be implemented
     *
     * @param connection the user connection
     */
    public static void sendServerDetails(final UserConnection connection, final String channel) {
        if (!Via.getConfig().sendServerDetails()) {
            return;
        }

        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        final ProtocolVersion serverVersion = protocolInfo.serverProtocolVersion();
        final JsonObject payload = new JsonObject();
        payload.addProperty("specVersion", VERSION);

        payload.addProperty("version", serverVersion.getOriginalVersion());
        payload.addProperty("versionName", serverVersion.getName());

        Via.getPlatform().sendCustomPayloadToClient(connection, channel, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

}
