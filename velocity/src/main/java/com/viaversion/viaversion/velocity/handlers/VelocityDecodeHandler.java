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
package com.viaversion.viaversion.velocity.handlers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

@ChannelHandler.Sharable
public class VelocityDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection info;

    public VelocityDecodeHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (!info.checkIncomingPacket(bytebuf.readableBytes())) throw CancelDecoderException.generate(null);
        if (!info.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), bytebuf);
        try {
            info.transformIncoming(transformedBuf, CancelDecoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelCodecException) return;
        super.exceptionCaught(ctx, cause);
    }

    // Abuse decoder handler to catch events
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event != VelocityChannelInitializer.COMPRESSION_ENABLED_EVENT) {
            super.userEventTriggered(ctx, event);
            return;
        }

        // When Velocity adds the compression handlers, the order becomes Minecraft Encoder -> Compressor -> Via Encoder
        // Move Via codec handlers back to right position
        ChannelPipeline pipeline = ctx.pipeline();

        ChannelHandler encoder = pipeline.remove(VelocityChannelInitializer.VIA_ENCODER);
        pipeline.addBefore(VelocityChannelInitializer.MINECRAFT_ENCODER, VelocityChannelInitializer.VIA_ENCODER, encoder);

        ChannelHandler decoder = pipeline.remove(VelocityChannelInitializer.VIA_DECODER);
        pipeline.addBefore(VelocityChannelInitializer.MINECRAFT_DECODER, VelocityChannelInitializer.VIA_DECODER, decoder);

        super.userEventTriggered(ctx, event);
    }
}
