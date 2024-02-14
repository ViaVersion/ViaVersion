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
package com.viaversion.viaversion.bukkit.listeners.protocol1_19to1_18_2;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener extends ViaBukkitListener {

    private static final Class<?> CRAFT_BLOCK_STATE_CLASS;

    static {
        try {
            CRAFT_BLOCK_STATE_CLASS = NMSUtil.obc("block.CraftBlockState");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockBreakListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_19To1_18_2.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (!event.isCancelled() || !isBlockEntity(block.getState())) {
            return;
        }

        // We need to resend the block entity data after an ack has been sent out
        final ProtocolVersion serverProtocolVersion = Via.getAPI().getServerVersion().highestSupportedProtocolVersion();
        final long delay = serverProtocolVersion.newerThan(ProtocolVersion.v1_8) && serverProtocolVersion.olderThan(ProtocolVersion.v1_14) ? 2 : 1;
        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            final BlockState state = block.getState();
            if (isBlockEntity(state)) {
                state.update(true, false);
            }
        }, delay);
    }

    private boolean isBlockEntity(final BlockState state) {
        // We love legacy versions
        return state.getClass() != CRAFT_BLOCK_STATE_CLASS;
    }
}
