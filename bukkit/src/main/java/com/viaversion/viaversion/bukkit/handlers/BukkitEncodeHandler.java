/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.handlers.ChannelHandlerContextWrapper;
import com.viaversion.viaversion.handlers.ViaCodecHandler;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class BukkitEncodeHandler extends MessageToByteEncoder implements ViaCodecHandler {
    private static Field versionField;

    static {
        try {
            // Attempt to get any version info from the handler
            versionField = NMSUtil.nms(
                    "PacketEncoder",
                    "net.minecraft.network.PacketEncoder"
            ).getDeclaredField("version");

            versionField.setAccessible(true);
        } catch (Exception e) {
            // Not compat version
        }
    }

    private final UserConnection info;
    private final MessageToByteEncoder minecraftEncoder;

    public BukkitEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, Object o, final ByteBuf bytebuf) throws Exception {
        if (versionField != null) {
            versionField.set(minecraftEncoder, versionField.get(this));
        }
        // handle the packet type
        if (!(o instanceof ByteBuf)) {
            // call minecraft encoder
            try {
                PipelineUtil.callEncode(this.minecraftEncoder, new ChannelHandlerContextWrapper(ctx, this), o, bytebuf);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }

        } else {
            bytebuf.writeBytes((ByteBuf) o);
        }
        transform(bytebuf);
    }

    @Override
    public void transform(ByteBuf bytebuf) throws Exception {
        if (!info.checkClientboundPacket()) throw CancelEncoderException.generate(null);
        if (!info.shouldTransformPacket()) return;
        info.transformClientbound(bytebuf, CancelEncoderException::generate);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) return; // ProtocolLib compat

        super.exceptionCaught(ctx, cause);
        if (!NMSUtil.isDebugPropertySet() && PipelineUtil.containsCause(cause, InformativeException.class)
                && (info.getProtocolInfo().getState() != State.HANDSHAKE || Via.getManager().isDebug())) {
            cause.printStackTrace(); // Print if CB doesn't already do it
        }
    }
}
