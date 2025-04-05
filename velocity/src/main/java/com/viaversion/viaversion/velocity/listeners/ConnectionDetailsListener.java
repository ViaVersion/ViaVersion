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
package com.viaversion.viaversion.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.ConnectionDetails;

public class ConnectionDetailsListener {
    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from(ConnectionDetails.PROXY_CHANNEL);

    @Subscribe
    public void onPostServerJoin(final ServerPostConnectEvent event) {
        final UserConnection connection = Via.getManager().getConnectionManager().getClientConnection(event.getPlayer().getUniqueId());
        if (connection != null) {
            ConnectionDetails.sendConnectionDetails(connection, ConnectionDetails.PROXY_CHANNEL);
        }
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        VelocityPlugin.PROXY.getChannelRegistrar().register(CHANNEL);
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (CHANNEL.equals(event.getIdentifier())) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}
