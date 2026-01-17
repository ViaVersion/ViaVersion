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
package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaChannelHandler;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class ViaCodecHandler extends ByteToMessageCodec<ByteBuf> implements ViaChannelHandler {

    public static final String NAME = "via-codec";

    protected final UserConnection connection;

    public ViaCodecHandler(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        if (!this.connection.checkOutgoingPacket()) {
            throw CancelEncoderException.generate(null);
        }

        out.writeBytes(in);
        if (this.connection.shouldTransformPacket()) {
            this.connection.transformOutgoing(out, CancelEncoderException::generate);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!this.connection.checkIncomingPacket(in.readableBytes())) {
            throw CancelDecoderException.generate(null);
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            if (this.connection.shouldTransformPacket()) {
                this.connection.transformIncoming(transformedBuf, CancelDecoderException::generate);
            }
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            super.write(ctx, msg, promise);
        } catch (Throwable e) {
            if (!(e instanceof CancelCodecException)) {
                throw e;
            } else {
                promise.setSuccess();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            super.channelRead(ctx, msg);
        } catch (Throwable e) {
            if (!(e instanceof CancelCodecException)) {
                throw e;
            }
        }
    }

    @Override
    public UserConnection connection() {
        return this.connection;
    }
}
