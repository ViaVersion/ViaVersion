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
package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;

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
        for (final UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
            if (!connection.isActive() || !connection.has(storableObject)) {
                continue;
            }

            connection.getChannel().eventLoop().execute(() -> {
                final T object = connection.get(storableObject);
                if (object != null) {
                    this.run(connection, object);
                }
            });
        }
    }
}
