/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
 * Optional utility to provide the target server of a player's connection with the player's native version and the
 * current platform name/version for version-specific handling.
 * <p>
 * Requires {@link ViaPlatform#sendCustomPayload(UserConnection, String, byte[])} to be implemented
 */
public final class ConnectionDetails {

    public static final String PROXY_CHANNEL = "vv:proxy_details";
    public static final String MOD_CHANNEL = "vv:mod_details";
    public static final String APP_CHANNEL = "vv:app_details";

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

}
