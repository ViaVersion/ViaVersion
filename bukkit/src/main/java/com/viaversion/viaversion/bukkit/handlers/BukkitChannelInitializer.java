/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.handlers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.classgenerator.ClassGenerator;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.classgenerator.generated.HandlerConstructor;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.lang.reflect.Method;

public class BukkitChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {

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

    public BukkitChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Deprecated/*(forRemoval = true)*/
    public ChannelInitializer<Channel> getOriginal() {
        return original;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Add originals
        INIT_CHANNEL_METHOD.invoke(this.original, channel);
        afterChannelInitialize(channel);
    }

    public static void afterChannelInitialize(Channel channel) {
        UserConnection connection = new UserConnectionImpl(channel);
        new ProtocolPipelineImpl(connection);

        if (PaperViaInjector.PAPER_PACKET_LIMITER) {
            connection.setPacketLimiterEnabled(false);
        }

        // Add our transformers
        HandlerConstructor constructor = ClassGenerator.getConstructor();
        MessageToByteEncoder encoder = constructor.newEncodeHandler(connection, (MessageToByteEncoder) channel.pipeline().get("encoder"));
        ByteToMessageDecoder decoder = constructor.newDecodeHandler(connection, (ByteToMessageDecoder) channel.pipeline().get("decoder"));

        channel.pipeline().replace("encoder", "encoder", encoder);
        channel.pipeline().replace("decoder", "decoder", decoder);
    }

    @Override
    public ChannelInitializer<Channel> original() {
        return original;
    }
}
