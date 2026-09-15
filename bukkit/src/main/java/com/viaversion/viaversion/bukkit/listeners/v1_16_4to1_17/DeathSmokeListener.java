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
package com.viaversion.viaversion.bukkit.listeners.v1_16_4to1_17;

import com.google.common.collect.Sets;
import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.Set;

public final class DeathSmokeListener extends ViaBukkitListener {
    private boolean trackerMethodExists;

    public DeathSmokeListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_16_4To1_17.class);

        try {
            Entity.class.getMethod("getTrackedPlayers");
            this.trackerMethodExists = true;
        } catch (NoSuchMethodException ignored) {}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        Via.getPlatform().runSync(() -> {
            Collection<Player> trackedByAndSelf;

            if (trackerMethodExists) {
                trackedByAndSelf = Sets.newHashSet(entity.getTrackedPlayers());

                if (entity instanceof Player player) {
                    trackedByAndSelf.add(player); // vanilla also sends it to themselves
                }
            } else {
                trackedByAndSelf = entity.getWorld().getPlayers();
            }

            for (Player viewer : trackedByAndSelf) {
                UserConnection viewerConnection = getUserConnection(viewer);

                if (!isOnPipe(viewerConnection)) {
                    continue;
                }

                PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_17.ENTITY_EVENT, null, viewerConnection);
                wrapper.write(Types.INT, entity.getEntityId()); // Entity ID
                wrapper.write(Types.BYTE, (byte) 60); // Event ID (60 = Death Smoke)

                wrapper.scheduleSend(Protocol1_16_4To1_17.class);
            }
        }, 20L); // 1.16.5 and below would handle this client side 20 ticks after death
    }
}
