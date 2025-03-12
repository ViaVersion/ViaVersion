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
package com.viaversion.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ViaListener {
    protected final Class<? extends Protocol> requiredPipeline;
    private boolean registered;

    protected ViaListener(Class<? extends Protocol> requiredPipeline) {
        this.requiredPipeline = requiredPipeline;
    }

    /**
     * Get the UserConnection from a UUID
     *
     * @param uuid UUID object
     * @return The UserConnection
     */
    protected @Nullable UserConnection getUserConnection(UUID uuid) {
        return Via.getManager().getConnectionManager().getServerConnection(uuid);
    }

    /**
     * Checks if the UUID is on the selected pipe
     *
     * @param uuid UUID Object
     * @return True if on pipe
     */
    protected boolean isOnPipe(UUID uuid) {
        return isOnPipe(getUserConnection(uuid));
    }

    protected boolean isOnPipe(UserConnection userConnection) {
        return userConnection != null &&
            (requiredPipeline == null || userConnection.getProtocolInfo().getPipeline().contains(requiredPipeline));
    }

    /**
     * Register the event
     */
    public abstract void register();

    protected boolean isRegistered() {
        return registered;
    }

    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
