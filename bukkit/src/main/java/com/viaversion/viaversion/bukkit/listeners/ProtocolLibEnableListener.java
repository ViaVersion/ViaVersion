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
package us.myles.ViaVersion.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.bukkit.platform.BukkitViaInjector;

public class ProtocolLibEnableListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        // Will likely never happen, but try to account for hacky plugin loading systems anyways
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(true);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals("ProtocolLib")) {
            ((BukkitViaInjector) Via.getManager().getInjector()).setProtocolLib(false);
        }
    }
}
