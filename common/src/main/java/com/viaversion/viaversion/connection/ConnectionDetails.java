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
 * connected players. See the documentation for more information:
 * <p>
 * <a href="https://github.com/ViaVersion/ViaVersion/wiki/Player-Details-Protocol">Player Details Protocol</a>
 * and
 * <a href="https://github.com/ViaVersion/ViaVersion/wiki/Server-Details-Protocol">Server Details Protocol</a>
 */
public final class ConnectionDetails {

    public static final String PROXY_CHANNEL = "vv:proxy_details";
    public static final String MOD_CHANNEL = "vv:mod_details";
    public static final String APP_CHANNEL = "vv:app_details";

    public static final String SERVER_CHANNEL = "vv:server_details";

    /**
     * Sends the running ViaVersion version and native version of a player to the proxy fronting the server.
     * <p>
     * Requires {@link ViaPlatform#sendCustomPayload(UserConnection, String, byte[])} to be implemented
     *
     * @param connection the user connection
     * @param channel    the channel to send the details to
     */
    public static void sendConnectionDetails(final UserConnection connection, final String channel) {
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
    public static void sendServerDetails(final UserConnection connection) {
        if (!Via.getConfig().sendServerDetails()) {
            return;
        }

        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        final ProtocolVersion serverVersion = protocolInfo.serverProtocolVersion();
        final JsonObject payload = new JsonObject();
        payload.addProperty("version", serverVersion.getOriginalVersion());
        payload.addProperty("versionName", serverVersion.getName());

        Via.getPlatform().sendCustomPayloadToClient(connection, SERVER_CHANNEL, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

}
