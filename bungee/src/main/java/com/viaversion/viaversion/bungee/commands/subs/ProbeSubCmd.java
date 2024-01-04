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
package com.viaversion.viaversion.bungee.commands.subs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.bungee.platform.BungeeViaConfig;

public class ProbeSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "probe";
    }

    @Override
    public String description() {
        return "Forces ViaVersion to scan server protocol versions " +
                (((BungeeViaConfig) Via.getConfig()).getBungeePingInterval() == -1 ?
                        "" : "(Also happens at an interval)");
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        Via.proxyPlatform().protocolDetectorService().probeAllServers();
        sendMessage(sender, "&6Started searching for protocol versions");
        return true;
    }
}
