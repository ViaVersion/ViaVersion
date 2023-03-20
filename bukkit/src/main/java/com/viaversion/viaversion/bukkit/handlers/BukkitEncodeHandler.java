/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@ChannelHandler.Sharable
public final class BukkitEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection connection;
    private boolean handledCompression = BukkitChannelInitializer.COMPRESSION_ENABLED_EVENT != null;

    public BukkitEncodeHandler(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf bytebuf, final List<Object> out) throws Exception {
        if (!connection.checkClientboundPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!connection.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            final boolean needsCompression = !handledCompression && handleCompressionOrder(ctx, transformedBuf);
            connection.transformClientbound(transformedBuf, CancelEncoderException::generate);
            if (needsCompression) {
                recompress(ctx, transformedBuf);
            }

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    private boolean handleCompressionOrder(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        final ChannelPipeline pipeline = ctx.pipeline();
        final List<String> names = pipeline.names();
        final int compressorIndex = names.indexOf(BukkitChannelInitializer.MINECRAFT_COMPRESSOR);
        if (compressorIndex == -1) {
            return false;
        }

        handledCompression = true;
        if (compressorIndex > names.indexOf(BukkitChannelInitializer.VIA_ENCODER)) {
            // Need to decompress this packet due to bad order
            final ByteBuf decompressed = (ByteBuf) PipelineUtil.callDecode((ByteToMessageDecoder) pipeline.get(BukkitChannelInitializer.MINECRAFT_DECOMPRESSOR), ctx, buf).get(0);
            try {
                buf.clear().writeBytes(decompressed);
            } finally {
                decompressed.release();
            }

            pipeline.addAfter(BukkitChannelInitializer.MINECRAFT_COMPRESSOR, BukkitChannelInitializer.VIA_ENCODER, pipeline.remove(BukkitChannelInitializer.VIA_ENCODER));
            pipeline.addAfter(BukkitChannelInitializer.MINECRAFT_DECOMPRESSOR, BukkitChannelInitializer.VIA_DECODER, pipeline.remove(BukkitChannelInitializer.VIA_DECODER));
            return true;
        }
        return false;
    }

    private void recompress(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        final ByteBuf compressed = ctx.alloc().buffer();
        try {
            PipelineUtil.callEncode((MessageToByteEncoder<ByteBuf>) ctx.pipeline().get(BukkitChannelInitializer.MINECRAFT_COMPRESSOR), ctx, buf, compressed);
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
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

    public UserConnection connection() {
        return connection;
    }
}
