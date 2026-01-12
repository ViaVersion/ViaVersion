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
package com.viaversion.viaversion.velocity.listeners;

import com.google.common.collect.Lists;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Fixes an issue where the Fabric Particle API causes disconnects when both the client and server have the mod installed and both are 1.21.5+.
// See https://github.com/ViaVersion/ViaFabric/issues/428
public final class FabricParticleListener {

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) {
            return;
        }

        final UserConnection connection = Via.getManager().getConnectionManager().getClientConnection(player.getUniqueId());
        final ProtocolVersion version = connection.getProtocolInfo().protocolVersion();
        final ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
        if (version.olderThan(ProtocolVersion.v1_21_5) || serverVersion.olderThan(ProtocolVersion.v1_21_5)) {
            return;
        }

        final String channel = event.getIdentifier().getId();
        if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
            final List<String> channels = Lists.newArrayList(new String(event.getData(), StandardCharsets.UTF_8).split("\0"));
            if (channels.remove("fabric:extended_block_state_particle_effect_sync")) {
                event.setResult(PluginMessageEvent.ForwardResult.handled());
                if (!channels.isEmpty()) {
                    System.out.println("Removed");
                    event.getTarget().sendPluginMessage(event.getIdentifier(), String.join("\0", channels).getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

}
