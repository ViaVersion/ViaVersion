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
package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;

/**
 * Named pipeline marker for native-protocol connections after login.
 * Forwards bytes unchanged so Via API send paths keep resolving {@code via-encoder}/{@code via-decoder}.
 */
@ChannelHandler.Sharable
public final class ViaPassthroughHandler extends ChannelDuplexHandler implements ViaChannelHandler {

    private final UserConnection connection;

    public ViaPassthroughHandler(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    public UserConnection connection() {
        return connection;
    }
}
