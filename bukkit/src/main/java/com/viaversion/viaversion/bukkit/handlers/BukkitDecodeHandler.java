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
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.ByteBufUtil;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

@ChannelHandler.Sharable
public final class BukkitDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection connection;

    public BukkitDecodeHandler(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf bytebuf, final List<Object> out) {
        if (!connection.checkServerboundPacket(bytebuf.readableBytes())) {
            throw CancelDecoderException.generate(null);
        }
        if (!connection.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        final ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), bytebuf);
        try {
            connection.transformIncoming(transformedBuf, CancelDecoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) {
            return;
        }

        super.exceptionCaught(ctx, cause);
        if (NMSUtil.isDebugPropertySet()) {
            return;
        }

        // Print if CB doesn't already do it
        final InformativeException exception = PipelineUtil.getCause(cause, InformativeException.class);
        if (exception != null && exception.shouldBePrinted()) {
            cause.printStackTrace();
            exception.setShouldBePrinted(false);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) throws Exception {
        if (BukkitChannelInitializer.COMPRESSION_ENABLED_EVENT == null || event != BukkitChannelInitializer.COMPRESSION_ENABLED_EVENT) {
            super.userEventTriggered(ctx, event);
            return;
        }

        // When compression handlers are added, the order becomes Minecraft Encoder -> Compressor -> Via Encoder; fix the order again
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addAfter(BukkitChannelInitializer.MINECRAFT_COMPRESSOR, BukkitChannelInitializer.VIA_ENCODER, pipeline.remove(BukkitChannelInitializer.VIA_ENCODER));
        pipeline.addAfter(BukkitChannelInitializer.MINECRAFT_DECOMPRESSOR, BukkitChannelInitializer.VIA_DECODER, pipeline.remove(BukkitChannelInitializer.VIA_DECODER));
        super.userEventTriggered(ctx, event);
    }
}
