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
package com.viaversion.viaversion.bukkit.listeners.v1_8to1_9;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class DeathListener extends ViaBukkitListener {

    public DeathListener(Plugin plugin) {
        super(plugin, Protocol1_8To1_9.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (isOnPipe(p) && Via.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld()) && e.getDeathMessage() != null)
            sendPacket(p, e.getDeathMessage());
    }

    public boolean checkGamerule(World w) {
        try {
            return Boolean.parseBoolean(w.getGameRuleValue("showDeathMessages"));
        } catch (Exception e) {
            return false;
        }
    }

    private void sendPacket(final Player p, final String msg) {
        Via.getPlatform().runSync(() -> {
            // If online
            UserConnection userConnection = getUserConnection(p);
            if (userConnection != null) {
                PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.PLAYER_COMBAT, userConnection);
                try {
                    wrapper.write(Types.VAR_INT, 2); // Event - Entity dead
                    wrapper.write(Types.VAR_INT, p.getEntityId()); // Player ID
                    wrapper.write(Types.INT, p.getEntityId()); // Entity ID
                    wrapper.write(Types.COMPONENT, ComponentUtil.plainToJson(msg)); // Message

                    wrapper.scheduleSend(Protocol1_8To1_9.class);
                } catch (Exception e) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send death message", e);
                }
            }
        });
    }
}
