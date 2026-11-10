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
package com.viaversion.viaversion.velocity.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaEncodeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class VelocityChannelInitializer extends ViaChannelInitializer {
    public static final String MINECRAFT_ENCODER = "minecraft-encoder";
    public static final String MINECRAFT_DECODER = "minecraft-decoder";
    public static final Object COMPRESSION_ENABLED_EVENT;

    static {
        try {
            final Class<?> eventClass = Class.forName("com.velocitypowered.proxy.protocol.VelocityConnectionEvent");
            COMPRESSION_ENABLED_EVENT = eventClass.getDeclaredField("COMPRESSION_ENABLED").get(null);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public VelocityChannelInitializer(final ChannelInitializer<Channel> original, final boolean clientSide) {
        super(original, clientSide);
    }

    @Override
    protected void injectPipeline(final ChannelPipeline pipeline, final UserConnection connection) {
        // We need to add a separated handler because Velocity uses pipeline().get(MINECRAFT_DECODER)
        final ViaInjector injector = Via.getManager().getInjector();
        pipeline.addBefore(MINECRAFT_ENCODER, injector.getEncoderName(), new ViaEncodeHandler(connection));
        pipeline.addBefore(MINECRAFT_DECODER, injector.getDecoderName(), new VelocityDecodeHandler(connection));
    }
}
