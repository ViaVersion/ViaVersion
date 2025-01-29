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
package com.viaversion.viaversion.protocols.v1_8to1_9.task;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocol.ProtocolRunnable;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;

public final class IdlePacketTask extends ProtocolRunnable {

    public IdlePacketTask() {
        super(Protocol1_8To1_9.class);
    }

    @Override
    public void run(final UserConnection connection) {
        final MovementTracker movementTracker = connection.get(MovementTracker.class);
        if (movementTracker == null) {
            return;
        }

        final long nextIdleUpdate = movementTracker.getNextIdlePacket();
        if (nextIdleUpdate <= System.currentTimeMillis()) {
            Via.getManager().getProviders().get(MovementTransmitterProvider.class).sendPlayer(connection);
        }
    }
}
