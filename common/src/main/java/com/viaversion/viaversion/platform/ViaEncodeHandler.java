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
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@ChannelHandler.Sharable
public class ViaEncodeHandler extends MessageToMessageEncoder<ByteBuf> implements ViaChannelHandler {

    public static final String NAME = "via-encoder";

    protected final UserConnection connection;

    public ViaEncodeHandler(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf buf, final List<Object> out) throws Exception {
        if (!connection.checkOutgoingPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!connection.shouldTransformPacket()) {
            out.add(buf.retain());
            return;
        }

        final ByteBuf transformedBuf = ByteBufUtil.copy(ctx.alloc(), buf);
        try {
            connection.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (!(cause instanceof CancelCodecException)) {
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    @Override
    public UserConnection connection() {
        return connection;
    }
}
