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
import com.viaversion.viaversion.platform.ViaDecodeHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

@ChannelHandler.Sharable
public class VelocityDecodeHandler extends ViaDecodeHandler {

    public VelocityDecodeHandler(final UserConnection connection) {
        super(connection);
    }

    // Abuse decoder handler to catch events
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) throws Exception {
        if (event == VelocityChannelInitializer.COMPRESSION_ENABLED_EVENT) {
            // When Velocity adds the compression handlers, the order becomes Minecraft Encoder -> Compressor -> Via Encoder
            // Move Via codec handlers back to right position
            final ViaInjector injector = Via.getManager().getInjector();
            final ChannelPipeline pipeline = ctx.pipeline();

            final ChannelHandler encoder = pipeline.remove(injector.getEncoderName());
            pipeline.addBefore(VelocityChannelInitializer.MINECRAFT_ENCODER, injector.getEncoderName(), encoder);

            final ChannelHandler decoder = pipeline.remove(injector.getDecoderName());
            pipeline.addBefore(VelocityChannelInitializer.MINECRAFT_DECODER, injector.getDecoderName(), decoder);
        }
        super.userEventTriggered(ctx, event);
    }
}
