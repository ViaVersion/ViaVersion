/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.listeners;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.platform.BukkitViaInjector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class ProtocolLibEnableListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        // Will likely never happen, but try to account for hacky plugin loading systems anyways
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            checkCompat(e.getPlugin());
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(false);
        }
    }

    public static void checkCompat(@Nullable Plugin protocolLib) {
        if (protocolLib != null) {
            String version = protocolLib.getDescription().getVersion();
            String majorVersion = version.split("\\.", 2)[0];
            try {
                // Only need the compat check for version < 5
                if (Integer.parseInt(majorVersion) < 5) {
                    ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(true);
                    return;
                }
            } catch (NumberFormatException ignored) {
                Via.getPlatform().getLogger().warning("ProtocolLib version check failed for version " + version);
            }
        }
        ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(false);
    }
}
