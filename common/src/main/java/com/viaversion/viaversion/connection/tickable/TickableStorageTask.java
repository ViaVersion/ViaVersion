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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import java.util.Map;

public final class TickableStorageTask implements Runnable {

    @Override
    public void run() {
        for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            final ProtocolInfo info = connection.getProtocolInfo();
            if (info == null) {
                continue;
            }

            // Don't run for 1.21.2+ clients on 1.21- servers, handled in the protocol directly
            if (info.getPipeline().contains(Protocol1_21To1_21_2.class)) {
                continue;
            }

            for (final Map.Entry<Class<?>, StorableObject> entry : connection.getStoredObjects().entrySet()) {
                if (entry.getValue() instanceof TickableStoredObject storableObject) {
                    if (info.getPipeline().contains(storableObject.protocolClass)) {
                        storableObject.serverTick();
                    }
                }
            }
        }
    }

}
