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
package com.viaversion.viaversion.bungee.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class BungeeChannelInitializer extends ChannelInitializer<Channel> {
    private final ChannelInitializer<Channel> original;
    private Method method;

    public BungeeChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to get initChannel method", e);
        }
    }

    @Override
    protected void initChannel(Channel socketChannel) throws Exception {
        if (!socketChannel.isActive()) {
            return;
        }

        UserConnection info = new UserConnectionImpl(socketChannel);
        // init protocol
        new ProtocolPipelineImpl(info);
        // Add originals
        this.method.invoke(this.original, socketChannel);

        if (!socketChannel.isActive()) return; // Don't inject if inactive
        if (socketChannel.pipeline().get("packet-encoder") == null) return; // Don't inject if no packet-encoder
        if (socketChannel.pipeline().get("packet-decoder") == null) return; // Don't inject if no packet-decoder
        // Add our transformers
        BungeeEncodeHandler encoder = new BungeeEncodeHandler(info);
        BungeeDecodeHandler decoder = new BungeeDecodeHandler(info);

        socketChannel.pipeline().addBefore("packet-encoder", "via-encoder", encoder);
        socketChannel.pipeline().addBefore("packet-decoder", "via-decoder", decoder);
    }

    public ChannelInitializer<Channel> getOriginal() {
        return original;
    }
}
