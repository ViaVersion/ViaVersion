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
package com.viaversion.viaversion.bukkit.listeners.v1_14_4to1_15;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import java.util.Arrays;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.potion.PotionEffectType;

public class EntityToggleGlideListener extends ViaBukkitListener {

    private boolean swimmingMethodExists;

    public EntityToggleGlideListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_14_4To1_15.class);
        try {
            Player.class.getMethod("isSwimming");
            swimmingMethodExists = true;
        } catch (NoSuchMethodException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!isOnPipe(player)) return;

        // Cancelling can only be done by updating the player's entity data
        if (event.isGliding() && event.isCancelled()) {
            PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_15.SET_ENTITY_DATA, null, getUserConnection(player));
            packet.write(Types.VAR_INT, player.getEntityId());

            byte bitmask = 0;
            // Collect other entity data for the mitmask
            if (player.getFireTicks() > 0) {
                bitmask |= 0x01;
            }
            if (player.isSneaking()) {
                bitmask |= 0x02;
            }
            // 0x04 is unused
            if (player.isSprinting()) {
                bitmask |= 0x08;
            }
            if (swimmingMethodExists && player.isSwimming()) {
                bitmask |= 0x10;
            }
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                bitmask |= 0x20;
            }
            if (player.isGlowing()) {
                bitmask |= 0x40;
            }

            // leave 0x80 as 0 to stop gliding
            packet.write(Types1_14.ENTITY_DATA_LIST, Arrays.asList(new EntityData(0, Types1_14.ENTITY_DATA_TYPES.byteType, bitmask)));
            packet.scheduleSend(Protocol1_14_4To1_15.class);
        }
    }
}
