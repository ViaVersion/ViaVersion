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
package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.ArrayList;
import java.util.List;

public class PlayerSubCmd implements ViaSubCommand {
    @Override
    public String name() {
        return "player";
    }

    @Override
    public String description() {
        return "Shows connection information about one or all players.";
    }

    @Override
    public String usage() {
        return "player <name|*>";
    }

    @Override
    public boolean execute(final ViaCommandSender sender, final String[] args) {
        if (args.length == 0) {
            return false;
        }

        final boolean all = args[0].equals("*");
        boolean any = false;
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            final ProtocolInfo info = connection.getProtocolInfo();
            if (args[0].equalsIgnoreCase(info.getUsername()) || all) {
                sendMessage(sender, "&7[&6" + info.getUsername() + "&7] UUID: &2" + info.getUuid() + " &7Client protocol: &2" + info.protocolVersion().getName() + " &7Server protocol: &2" + info.serverProtocolVersion().getName() + " &7Client: &2" + connection.isClientSide());
                any = true;
            }
        }
        if (!any) {
            sendMessage(sender, all ? "&cNo players found!" : "&cNo player found with the name: " + args[0]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(final ViaCommandSender sender, final String[] args) {
        if (args.length == 1) {
            final String input = args[0].toLowerCase();

            final List<String> matches = new ArrayList<>();
            for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
                final String name = connection.getProtocolInfo().getUsername();
                if (input.isEmpty() || name.toLowerCase().startsWith(input)) {
                    matches.add(name);
                }
            }
            matches.add("*");
            return matches;
        }
        return ViaSubCommand.super.onTabComplete(sender, args);
    }
}
