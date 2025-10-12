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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ScoreboardTeamStorage implements StorableObject {

    private final Map<String, Set<String>> teams = new HashMap<>();

    public void createTeam(final String name) {
        if (this.teams.containsKey(name)) {
            return;
        }

        this.teams.put(name, new HashSet<>());
    }

    public void removeTeam(final String name) {
        this.teams.remove(name);
    }

    public void addPlayerToTeam(final String team, final String[] player) {
        final Set<String> players = this.teams.get(team);
        if (players == null) {
            return;
        }

        for (final Set<String> allPlayers : this.teams.values()) {
            for (final String toRemove : player) {
                allPlayers.remove(toRemove);
            }
        }
        Collections.addAll(players, player);
    }

    public void removeFromTeam(final String team, final String player) {
        final Set<String> players = this.teams.get(team);
        if (players != null) {
            players.remove(player);
        }
    }

    public String getPlayerTeam(final String player) {
        for (final Map.Entry<String, Set<String>> entry : this.teams.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
