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
package com.viaversion.viaversion.velocity.providers;

import com.velocitypowered.api.proxy.ServerConnection;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import com.viaversion.viaversion.velocity.platform.VelocityViaInjector;
import com.viaversion.viaversion.velocity.service.ProtocolDetectorService;
import io.netty.channel.ChannelHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

public class VelocityVersionProvider extends BaseVersionProvider {
    private static Method getAssociation;

    static {
        try {
            getAssociation = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection").getMethod("getAssociation");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getClosestServerProtocol(UserConnection user) throws Exception {
        return user.isClientSide() ? getBackProtocol(user) : getFrontProtocol(user);
    }

    private int getBackProtocol(UserConnection user) throws Exception {
        //TODO use newly added Velocity netty event
        ChannelHandler mcHandler = user.getChannel().pipeline().get("handler");
        return ProtocolDetectorService.getProtocolId(
                ((ServerConnection) getAssociation.invoke(mcHandler)).getServerInfo().getName());
    }

    private int getFrontProtocol(UserConnection user) throws Exception {
        int playerVersion = user.getProtocolInfo().getProtocolVersion();

        IntStream versions = com.velocitypowered.api.network.ProtocolVersion.SUPPORTED_VERSIONS.stream()
                .mapToInt(com.velocitypowered.api.network.ProtocolVersion::getProtocol);

        // Modern forwarding mode needs 1.13 Login plugin message
        if (VelocityViaInjector.getPlayerInfoForwardingMode != null
                && ((Enum<?>) VelocityViaInjector.getPlayerInfoForwardingMode.invoke(VelocityPlugin.PROXY.getConfiguration()))
                .name().equals("MODERN")) {
            versions = versions.filter(ver -> ver >= ProtocolVersion.v1_13.getVersion());
        }
        int[] compatibleProtocols = versions.toArray();

        // Bungee supports it
        if (Arrays.binarySearch(compatibleProtocols, playerVersion) >= 0)
            return playerVersion;

        // Older than bungee supports, get the lowest version
        if (playerVersion < compatibleProtocols[0]) {
            return compatibleProtocols[0];
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (int i = compatibleProtocols.length - 1; i >= 0; i--) {
            int protocol = compatibleProtocols[i];
            if (playerVersion > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + playerVersion);
        return playerVersion;
    }
}
