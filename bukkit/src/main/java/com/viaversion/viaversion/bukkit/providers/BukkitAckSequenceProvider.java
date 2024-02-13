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
package com.viaversion.viaversion.bukkit.providers;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.tasks.protocol1_19to1_18_2.AckSequenceTask;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage.SequenceStorage;

public final class BukkitAckSequenceProvider extends AckSequenceProvider {

    private final ViaVersionPlugin plugin;

    public BukkitAckSequenceProvider(final ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleSequence(final UserConnection connection, final int sequence) {
        final SequenceStorage sequenceStorage = connection.get(SequenceStorage.class);
        final int previousSequence = sequenceStorage.setSequenceId(sequence);
        if (previousSequence == -1) {
            final ProtocolVersion serverProtocolVersion = connection.getProtocolInfo().serverProtocolVersion();
            final long delay = serverProtocolVersion.newerThan(ProtocolVersion.v1_8) && serverProtocolVersion.olderThan(ProtocolVersion.v1_14) ? 2 : 1;

            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new AckSequenceTask(connection, sequenceStorage), delay);
            }
        }
    }
}