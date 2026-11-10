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
package com.viaversion.viaversion.protocols.v1_8to1_9.provider;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionProvider implements Provider {
    public void handlePlayCompression(UserConnection user, int threshold) {
        if (!user.isClientSide()) {
            throw new IllegalStateException("PLAY state Compression packet is unsupported");
        }

        ChannelPipeline pipe = user.getChannel().pipeline();
        if (threshold < 0) {
            removeHandlers(pipe);
            return;
        }

        if (pipe.get(getCompressName()) instanceof CompressionHandler compressionHandler) {
            compressionHandler.setCompressionThreshold(threshold);
            ((CompressionHandler) pipe.get(getDecompressName())).setCompressionThreshold(threshold);
            return;
        }

        removeHandlers(pipe);
        pipe.addBefore(Via.getManager().getInjector().getEncoderName(), getCompressName(), getEncoder(threshold));
        pipe.addBefore(Via.getManager().getInjector().getDecoderName(), getDecompressName(), getDecoder(threshold));
    }

    private void removeHandlers(ChannelPipeline pipeline) {
        if (pipeline.get(getCompressName()) != null) {
            pipeline.remove(getCompressName());
            pipeline.remove(getDecompressName());
        }
    }

    protected CompressionHandler getEncoder(int threshold) {
        return new Compressor(threshold);
    }

    protected CompressionHandler getDecoder(int threshold) {
        return new Decompressor(threshold);
    }

    protected String getCompressName() {
        return "compress";
    }

    protected String getDecompressName() {
        return "decompress";
    }

    public interface CompressionHandler extends ChannelHandler {
        void setCompressionThreshold(int threshold);
    }

    private static class Decompressor extends MessageToMessageDecoder<ByteBuf> implements CompressionHandler {

        private final Inflater inflater;
        private int threshold;

        public Decompressor(int var1) {
            this.threshold = var1;
            this.inflater = new Inflater();
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (!in.isReadable()) return;

            int outLength = Types.VAR_INT.readPrimitive(in);
            if (outLength == 0) {
                out.add(in.readBytes(in.readableBytes()));
                return;
            }

            if (outLength < this.threshold) {
                throw new DecoderException("Badly compressed packet - size of " + outLength + " is below server threshold of " + this.threshold);
            } else if (outLength > 2097152) {
                throw new DecoderException("Badly compressed packet - size of " + outLength + " is larger than protocol maximum of " + 2097152);
            }

            ByteBuf temp = in;
            if (!in.hasArray()) {
                temp = ctx.alloc().heapBuffer().writeBytes(in);
            } else {
                in.retain();
            }
            ByteBuf output = ctx.alloc().heapBuffer(outLength, outLength);
            try {
                this.inflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
                output.writerIndex(output.writerIndex() + this.inflater.inflate(
                    output.array(), output.arrayOffset(), outLength));
                out.add(output.retain());
            } finally {
                output.release();
                temp.release();
                this.inflater.reset();
            }
        }

        @Override
        public void setCompressionThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

    private static class Compressor extends MessageToByteEncoder<ByteBuf> implements CompressionHandler {

        private final Deflater deflater;
        private int threshold;

        public Compressor(int var1) {
            this.threshold = var1;
            this.deflater = new Deflater();
        }

        protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
            int frameLength = in.readableBytes();
            if (frameLength < this.threshold) {
                out.writeByte(0); // varint
                out.writeBytes(in);
                return;
            }

            Types.VAR_INT.writePrimitive(out, frameLength);

            ByteBuf temp = in;
            if (!in.hasArray()) {
                temp = ctx.alloc().heapBuffer().writeBytes(in);
            } else {
                in.retain();
            }
            ByteBuf output = ctx.alloc().heapBuffer();
            try {
                this.deflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
                deflater.finish();

                while (!deflater.finished()) {
                    output.ensureWritable(4096);
                    output.writerIndex(output.writerIndex() + this.deflater.deflate(output.array(),
                        output.arrayOffset() + output.writerIndex(), output.writableBytes()));
                }
                out.writeBytes(output);
            } finally {
                output.release();
                temp.release();
                this.deflater.reset();
            }
        }

        @Override
        public void setCompressionThreshold(int threshold) {
            this.threshold = threshold;
        }
    }
}
