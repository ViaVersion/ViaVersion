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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PPSSubCmd extends ViaSubCommand {
    @Override
    public String name() {
        return "pps";
    }

    @Override
    public String description() {
        return "Shows the packets per second of online players.";
    }

    @Override
    public String usage() {
        return "pps";
    }

    @Override
    public boolean execute(ViaCommandSender sender, String[] args) {
        Map<Integer, Set<String>> playerVersions = new HashMap<>();
        int totalPackets = 0;
        int clients = 0;
        long max = 0;

        for (ViaCommandSender p : Via.getPlatform().getOnlinePlayers()) {
            int playerVersion = Via.getAPI().getPlayerVersion(p.getUUID());
            if (!playerVersions.containsKey(playerVersion))
                playerVersions.put(playerVersion, new HashSet<>());
            UserConnection uc = Via.getManager().getConnectionManager().getConnectedClient(p.getUUID());
            if (uc != null && uc.getPacketTracker().getPacketsPerSecond() > -1) {
                playerVersions.get(playerVersion).add(p.getName() + " (" + uc.getPacketTracker().getPacketsPerSecond() + " PPS)");
                totalPackets += uc.getPacketTracker().getPacketsPerSecond();
                if (uc.getPacketTracker().getPacketsPerSecond() > max) {
                    max = uc.getPacketTracker().getPacketsPerSecond();
                }
                clients++;
            }
        }
        Map<Integer, Set<String>> sorted = new TreeMap<>(playerVersions);
        sendMessage(sender, "&4Live Packets Per Second");
        if (clients > 1) {
            sendMessage(sender, "&cAverage: &f" + (totalPackets / clients));
            sendMessage(sender, "&cHighest: &f" + max);
        }
        if (clients == 0) {
            sendMessage(sender, "&cNo clients to display.");
        }
        for (Map.Entry<Integer, Set<String>> entry : sorted.entrySet())
            sendMessage(sender, "&8[&6%s&8]: &b%s", ProtocolVersion.getProtocol(entry.getKey()).getName(), entry.getValue());
        sorted.clear();
        return true;
    }
}
