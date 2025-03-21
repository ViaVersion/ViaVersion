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
package com.viaversion.viaversion.bukkit.listeners.v1_21_4to1_21_5;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public final class ContainerClickDesyncCheck extends ViaBukkitListener {

    private static final Method SIMPLIFY_DESYNC_CHECK_METHOD;

    static {
        try {
            SIMPLIFY_DESYNC_CHECK_METHOD = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("setSimplifyContainerDesyncCheck", Boolean.TYPE);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public ContainerClickDesyncCheck(final ViaVersionPlugin plugin) {
        super(plugin, Protocol1_21_4To1_21_5.class);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!isOnPipe(player)) {
            return;
        }

        // Reduce desync checks to only item ids and amount, skipping data equality checks
        try {
            SIMPLIFY_DESYNC_CHECK_METHOD.invoke(player, true);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
