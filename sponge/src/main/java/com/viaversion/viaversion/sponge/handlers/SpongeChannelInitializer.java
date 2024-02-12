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
package com.viaversion.viaversion.sponge.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;

public class SpongeChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {

    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public SpongeChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Ensure ViaVersion is loaded
        if (Via.getAPI().getServerVersion().isKnown()
                && channel instanceof SocketChannel) { // channel can be LocalChannel on internal server
            UserConnection info = new UserConnectionImpl(channel);
            // init protocol
            new ProtocolPipelineImpl(info);
            // Add originals
            INIT_CHANNEL_METHOD.invoke(this.original, channel);
            // Add our transformers
            MessageToByteEncoder encoder = new SpongeEncodeHandler(info, (MessageToByteEncoder) channel.pipeline().get("encoder"));
            ByteToMessageDecoder decoder = new SpongeDecodeHandler(info, (ByteToMessageDecoder) channel.pipeline().get("decoder"));

            channel.pipeline().replace("encoder", "encoder", encoder);
            channel.pipeline().replace("decoder", "decoder", decoder);
        } else {
            INIT_CHANNEL_METHOD.invoke(this.original, channel);
        }
    }

    @Override
    public ChannelInitializer<Channel> original() {
        return original;
    }
}
