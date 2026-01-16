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
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Method;

public abstract class ViaChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {

    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;
    private final boolean clientSide;

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected ViaChannelInitializer(final ChannelInitializer<Channel> original, final boolean clientSide) {
        this.original = original;
        this.clientSide = clientSide;
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        INIT_CHANNEL_METHOD.invoke(this.original, channel);

        final UserConnection connection = createUserConnection(channel, this.clientSide);
        injectPipeline(channel.pipeline(), connection);
    }

    public static UserConnection createUserConnection(final Channel channel, final boolean clientSide) {
        final UserConnection connection = new UserConnectionImpl(channel, clientSide);
        new ProtocolPipelineImpl(connection);
        return connection;
    }

    protected abstract void injectPipeline(ChannelPipeline pipeline, UserConnection connection);

    @Override
    public ChannelInitializer<Channel> original() {
        return original;
    }

    public boolean clientSide() {
        return clientSide;
    }
}
