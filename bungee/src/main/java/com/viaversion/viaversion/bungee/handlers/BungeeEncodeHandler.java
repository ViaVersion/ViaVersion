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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bungee.util.BungeePipelineUtil;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@ChannelHandler.Sharable
public class BungeeEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression;

    public BungeeEncodeHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            throw CancelEncoderException.generate(null);
        }

        if (!info.checkClientboundPacket()) throw CancelEncoderException.generate(null);
        if (!info.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            boolean needsCompress = handleCompressionOrder(ctx, transformedBuf);
            info.transformClientbound(transformedBuf, CancelEncoderException::generate);

            if (needsCompress) {
                recompress(ctx, transformedBuf);
            }

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf buf) {
        boolean needsCompress = false;
        if (!handledCompression && ctx.pipeline().names().indexOf("compress") > ctx.pipeline().names().indexOf("via-encoder")) {
            // Need to decompress this packet due to bad order
            ByteBuf decompressed = BungeePipelineUtil.decompress(ctx, buf);

            // Ensure the buffer wasn't reused
            if (buf != decompressed) {
                try {
                    buf.clear().writeBytes(decompressed);
                } finally {
                    decompressed.release();
                }
            }

            // Reorder the pipeline
            ChannelHandler decoder = ctx.pipeline().get("via-decoder");
            ChannelHandler encoder = ctx.pipeline().get("via-encoder");
            ctx.pipeline().remove(decoder);
            ctx.pipeline().remove(encoder);
            ctx.pipeline().addAfter("decompress", "via-decoder", decoder);
            ctx.pipeline().addAfter("compress", "via-encoder", encoder);
            needsCompress = true;
            handledCompression = true;
        }
        return needsCompress;
    }

    private void recompress(ChannelHandlerContext ctx, ByteBuf buf) {
        ByteBuf compressed = BungeePipelineUtil.compress(ctx, buf);
        try {
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelCodecException) return;
        super.exceptionCaught(ctx, cause);
    }
}
