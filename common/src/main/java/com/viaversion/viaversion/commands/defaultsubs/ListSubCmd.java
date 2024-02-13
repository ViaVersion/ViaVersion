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
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ListSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Shows lists of the versions from logged in players.";
    }

    @Override
    public String usage() {
        return "list";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        Map<ProtocolVersion, Set<String>> playerVersions = new TreeMap<>(ProtocolVersion::compareTo);

        for (ViaCommandSender p : Via.getPlatform().getOnlinePlayers()) {
            int playerVersion = Via.getAPI().getPlayerVersion(p.getUUID());
            ProtocolVersion key = ProtocolVersion.getProtocol(playerVersion);
            playerVersions.computeIfAbsent(key, s -> new HashSet<>()).add(p.getName());
        }

        for (Map.Entry<ProtocolVersion, Set<String>> entry : playerVersions.entrySet()) {
            sendMessage(sender, "&8[&6%s&8] (&7%d&8): &b%s", entry.getKey().getName(), entry.getValue().size(), entry.getValue());
        }

        playerVersions.clear();
        return true;
    }
}
