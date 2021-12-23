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
package com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.sponge5;

import com.viaversion.viaversion.SpongePlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ArmorType;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.sponge.listeners.ViaSpongeListener;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.UUID;

public class Sponge5ArmorListener extends ViaSpongeListener {
    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

    public Sponge5ArmorListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    //
    public void sendArmorUpdate(Player player) {
        // Ensure that the player is on our pipe
        if (!isOnPipe(player.uniqueId())) return;


        int armor = 0;
        armor += calculate(player.head());
        armor += calculate(player.chest());
        armor += calculate(player.legs());
        armor += calculate(player.feet());

        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_PROPERTIES, null, getUserConnection(player.getUniqueId()));
        try {
            wrapper.write(Type.VAR_INT, getEntityId(player)); // Player ID
            wrapper.write(Type.INT, 1); // only 1 property
            wrapper.write(Type.STRING, "generic.armor");
            wrapper.write(Type.DOUBLE, 0D); //default 0 armor
            wrapper.write(Type.VAR_INT, 1); // 1 modifier
            wrapper.write(Type.UUID, ARMOR_ATTRIBUTE); // armor modifier uuid
            wrapper.write(Type.DOUBLE, (double) armor); // the modifier value
            wrapper.write(Type.BYTE, (byte) 0);// the modifier operation, 0 is add number

            wrapper.scheduleSend(Protocol1_9To1_8.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculate(ItemStack itemStack) {
        if (itemStack != null) {
            // todo
            return ArmorType.findByType(itemStack.type()).getArmorPoints();
        }
        return 0;
    }

    @Listener
    public void onContainerClick(ClickContainerEvent e, @Root Player player) {
        for (SlotTransaction transaction : e.transactions()) {
            if (ArmorType.isArmor(transaction.finalReplacement().type()) ||
                    ArmorType.isArmor(e.cursorTransaction().finalReplacement().type())) {
                sendDelayedArmorUpdate(player);
                break;
            }
        }
    }

    @Listener
    public void onInteract(InteractEvent event, @Root Player player) {
        if (player.itemInHand(HandTypes.MAIN_HAND) != null) {
            if (ArmorType.isArmor(player.itemInHand(HandTypes.MAIN_HAND).type()))
                sendDelayedArmorUpdate(player);
        }
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join e) {
        sendArmorUpdate(e.player());
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent e) {
        sendDelayedArmorUpdate(e.entity());
    }

    @Listener
    public void onWorldChange(MoveEntityEvent e) {
        if (!(e.entity() instanceof Player)) return;
        if (!e.cause().contains(MovementTypes.ENTITY_TELEPORT)) return; //todo: probably doesn't work

        if (!e.originalDestinationPosition().getExtent().getUniqueId().equals(e.destinationPosition().getExtent().getUniqueId())) {
            sendArmorUpdate((Player) e.entity());
        }
    }

    public void sendDelayedArmorUpdate(final Player player) {
        if (!isOnPipe(player.uniqueId())) return; // Don't start a task if the player is not on the pipe
        Via.getPlatform().runSync(new Runnable() {
            @Override
            public void run() {
                sendArmorUpdate(player);
            }
        });
    }
}
