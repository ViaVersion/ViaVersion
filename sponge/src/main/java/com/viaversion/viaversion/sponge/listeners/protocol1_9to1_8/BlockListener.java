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
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.sponge.listeners.ViaSpongeListener;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;

public class BlockListener extends ViaSpongeListener {

    public BlockListener(SpongePlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    @Listener
    public void placeBlock(ChangeBlockEvent.Place e, @Root Player player) {
        if (isOnPipe(player.getUniqueId())) {
            Location loc = e.getTransactions().get(0).getFinal().getLocation().get();
            EntityTracker1_9 tracker = getUserConnection(player.getUniqueId()).getEntityTracker(Protocol1_9To1_8.class);
            tracker.addBlockInteraction(new Position(loc.getBlockX(), (short) loc.getBlockY(), loc.getBlockZ()));
        }
    }
}
