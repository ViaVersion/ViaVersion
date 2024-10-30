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
package com.viaversion.viaversion.connection.tickable;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.scheduler.TaskScheduler;

/**
 * Optional {@link StoredObject} that calls a tick method every tick.
 * <p>
 * Either called by {@link #serverTick()} which is implemented using the {@link TaskScheduler} API
 * or the {@link #clientTick()} which is called by the client end tick packet for 1.21.2+ clients.
 * <p>
 * Limitation:
 * {@link #clientTick()} will only work for 1.21.2+ clients and only on 1.21- servers. For newer protocols, it's intended
 * to just register a {@link ServerboundPackets1_21_2#CLIENT_TICK_END} handler directly.
 */
public class TickableStoredObject extends StoredObject {

    protected final Class<? extends Protocol<?, ?, ?, ?>> protocolClass;

    protected TickableStoredObject(final Class<? extends Protocol<?, ?, ?, ?>> protocolClass, final UserConnection user) {
        super(user);
        this.protocolClass = protocolClass;
    }

    /**
     * Called by {@link TickableStorageTask} every tick.
     */
    public void serverTick() {
        tick();
    }

    /**
     * Called for 1.21.2+ clients on older servers when the client sends a {@link ServerboundPackets1_21_2#CLIENT_TICK_END} packet.
     */
    public void clientTick() {
        tick();
    }

    /**
     * Called every tick. Either by {@link #serverTick()} or {@link #clientTick()}.
     */
    public void tick() {
    }

    public Class<? extends Protocol<?, ?, ?, ?>> protocolClass() {
        return protocolClass;
    }

}
