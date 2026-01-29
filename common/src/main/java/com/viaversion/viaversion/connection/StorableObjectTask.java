/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.util.concurrent.EventExecutor;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of {@link Runnable} that will run {@link #run(UserConnection, StorableObject)} for all active user connections.
 */
public abstract class StorableObjectTask<T extends StorableObject> implements Runnable {

    private final Class<T> storableObject;

    protected StorableObjectTask(final Class<T> storableObject) {
        this.storableObject = storableObject;
    }

    public abstract void run(final UserConnection connection, final T storableObject);

    @Override
    public void run() {
        // Group connections by event loop to batch executions and reduce wakeup() calls
        final Map<EventExecutor, List<UserConnection>> connectionsByEventLoop = new IdentityHashMap<>();
        
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            if (!connection.isActive() || !connection.has(storableObject)) {
                continue;
            }
            
            final EventExecutor eventLoop = connection.getChannel().eventLoop();
            connectionsByEventLoop.computeIfAbsent(eventLoop, k -> new ArrayList<>()).add(connection);
        }
        
        // Execute batched tasks - one execute() call per event loop instead of per connection
        for (final Map.Entry<EventExecutor, List<UserConnection>> entry : connectionsByEventLoop.entrySet()) {
            final List<UserConnection> connections = entry.getValue();
            entry.getKey().execute(() -> {
                for (final UserConnection connection : connections) {
                    try {
                        final T object = connection.get(storableObject);
                        if (object != null) {
                            this.run(connection, object);
                        }
                    } catch (final Exception e) {
                        Via.getPlatform().getLogger().warning("Error running task for connection: " + e.getMessage());
                    }
                }
            });
        }
    }
}
