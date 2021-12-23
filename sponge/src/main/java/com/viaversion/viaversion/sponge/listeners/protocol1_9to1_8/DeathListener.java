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
package com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8;

import com.viaversion.viaversion.SpongePlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.sponge.listeners.ViaSpongeListener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class DeathListener extends ViaSpongeListener {
    public DeathListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    @Listener(order = Order.LAST)
    public void onDeath(DestructEntityEvent.Death e) {
        if (!(e.entity() instanceof Player))
            return;

        Player p = (Player) e.entity();
        if (isOnPipe(p.uniqueId()) && Via.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld())) {
            sendPacket(p, PlainTextComponentSerializer.plainText().serialize(e.message()));
        }
    }

    public boolean checkGamerule(World w) {
        Optional<String> gamerule = w.gameRule("showDeathMessages");

        if (gamerule.isPresent()) {
            try {
                return Boolean.parseBoolean(gamerule.get());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private void sendPacket(final Player p, final String msg) {
        Via.getPlatform().runSync(new Runnable() {
            @Override
            public void run() {
                PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.COMBAT_EVENT, null, getUserConnection(p.getUniqueId()));
                try {
                    int entityId = getEntityId(p);
                    wrapper.write(Type.VAR_INT, 2); // Event - Entity dead
                    wrapper.write(Type.VAR_INT, entityId); // Player ID
                    wrapper.write(Type.INT, entityId); // Entity ID
                    Protocol1_9To1_8.FIX_JSON.write(wrapper, msg); // Message

                    wrapper.scheduleSend(Protocol1_9To1_8.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
