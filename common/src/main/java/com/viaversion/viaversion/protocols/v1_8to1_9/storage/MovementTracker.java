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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.tickable.TickableStoredObject;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;

public class MovementTracker extends TickableStoredObject {
    private static final long IDLE_PACKET_DELAY = 50L; // Update every 50ms (20tps)
    private static final long IDLE_PACKET_LIMIT = 20; // Max 20 ticks behind
    private long nextIdlePacket;
    private boolean ground;

    public MovementTracker(final UserConnection user) {
        super(Protocol1_8To1_9.class, user);
    }

    public void incrementIdlePacket() {
        // Notify of next update
        // Allow a maximum lag spike of 1 second (20 ticks/updates)
        this.nextIdlePacket = Math.max(nextIdlePacket + IDLE_PACKET_DELAY, System.currentTimeMillis() - IDLE_PACKET_DELAY * IDLE_PACKET_LIMIT);
    }

    @Override
    public void serverTick() {
        if (nextIdlePacket <= System.currentTimeMillis() && user().getChannel().isOpen()) {
            tick();
        }
    }

    @Override
    public void tick() {
        // Send on tick end for 1.21.2+ clients
        Via.getManager().getProviders().get(MovementTransmitterProvider.class).sendPlayer(user());
    }

    public long getNextIdlePacket() {
        return nextIdlePacket;
    }

    public boolean isGround() {
        return ground;
    }

    public void setGround(boolean ground) {
        this.ground = ground;
    }
}
