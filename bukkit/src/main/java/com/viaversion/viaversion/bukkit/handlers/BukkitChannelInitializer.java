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
package com.viaversion.viaversion.bukkit.handlers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BukkitChannelInitializer extends ChannelInitializer<Channel> implements WrappedChannelInitializer {

    public static final String VIA_ENCODER = "via-encoder";
    public static final String VIA_DECODER = "via-decoder";
    public static final String MINECRAFT_ENCODER = "encoder";
    public static final String MINECRAFT_DECODER = "decoder";
    public static final String MINECRAFT_OUTBOUND_CONFIG = "outbound_config";
    public static final String MINECRAFT_COMPRESSOR = "compress";
    public static final String MINECRAFT_DECOMPRESSOR = "decompress";
    public static final Object COMPRESSION_ENABLED_EVENT = paperCompressionEnabledEvent();
    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static @Nullable Object paperCompressionEnabledEvent() {
        try {
            final Class<?> eventClass = Class.forName("io.papermc.paper.network.ConnectionEvent");
            return eventClass.getDeclaredField("COMPRESSION_THRESHOLD_SET").get(null);
        } catch (final ReflectiveOperationException e) {
            return null;
        }
    }

    public BukkitChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Add originals
        INIT_CHANNEL_METHOD.invoke(this.original, channel);
        afterChannelInitialize(channel);
    }

    public static void afterChannelInitialize(Channel channel) {
        final UserConnection connection = new UserConnectionImpl(channel);
        new ProtocolPipelineImpl(connection);

        // Add our transformers
        final ChannelPipeline pipeline = channel.pipeline();
        final String encoderName = pipeline.get(MINECRAFT_OUTBOUND_CONFIG) != null ? MINECRAFT_OUTBOUND_CONFIG : MINECRAFT_ENCODER;
        pipeline.addBefore(encoderName, VIA_ENCODER, new BukkitEncodeHandler(connection));
        pipeline.addBefore(MINECRAFT_DECODER, VIA_DECODER, new BukkitDecodeHandler(connection));
    }

    @Override
    public ChannelInitializer<Channel> original() {
        return original;
    }
}
