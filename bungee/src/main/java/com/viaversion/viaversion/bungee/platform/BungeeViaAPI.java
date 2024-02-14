/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.bungee.platform;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bungee.service.ProtocolDetectorService;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeViaAPI extends ViaAPIBase<ProxiedPlayer> {

    @Override
    public ProtocolVersion getPlayerProtocolVersion(ProxiedPlayer player) {
        return getPlayerProtocolVersion(player.getUniqueId());
    }

    @Override
    public void sendRawPacket(ProxiedPlayer player, ByteBuf packet) throws IllegalArgumentException {
        sendRawPacket(player.getUniqueId(), packet);
    }

    /**
     * Forces ViaVersion to probe a server
     *
     * @param serverInfo The serverinfo to probe
     */
    public void probeServer(ServerInfo serverInfo) {
        ((ProtocolDetectorService) Via.proxyPlatform().protocolDetectorService()).probeServer(serverInfo);
    }
}
