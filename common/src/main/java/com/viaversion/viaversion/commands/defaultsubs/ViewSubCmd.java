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
package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.List;

public class ViewSubCmd implements ViaSubCommand {
    @Override
    public String name() {
        return "view";
    }

    @Override
    public String description() {
        return "Shows statistics about a specific player.";
    }

    @Override
    public String usage() {
        return "view <player|*>";
    }

    @Override
    public boolean execute(final ViaCommandSender sender, final String[] args) {
        if (args.length == 0) {
            return false;
        }
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            final ProtocolInfo info = connection.getProtocolInfo();
            if (args[0].equalsIgnoreCase(info.getUsername()) || args[0].equals("*")) {
                sendMessage(sender, "&7[&6" + info.getUsername() + "&7] UUID: &5" + info.getUuid() +
                    " &7Client-Protocol: &5" + info.protocolVersion().getName() + " &7Server-Protocol: &5" + info.serverProtocolVersion().getName());
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(final ViaCommandSender sender, final String[] args) {
        if (args.length == 1) {
            final List<String> matches = new ArrayList<>();
            for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
                final String name = connection.getProtocolInfo().getUsername();
                if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                    matches.add(name);
                }
            }
            if (matches.isEmpty()) {
                matches.add("*");
            }
            return matches;
        }
        return ViaSubCommand.super.onTabComplete(sender, args);
    }
}
