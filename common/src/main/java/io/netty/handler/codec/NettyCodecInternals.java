package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public final class NettyCodecInternals {

    private NettyCodecInternals() { }

    public static <T> List<Object> callDecode(MessageToMessageDecoder handler, ChannelHandlerContext ctx, T msg, List<Object> out) throws Exception {
        handler.decode(ctx, msg, out);
        return out;
    }

    public static List<Object> callDecode(ByteToMessageDecoder handler, ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        handler.decode(ctx, msg, out);
        return out;
    }

    public static <T> ByteBuf callEncode(MessageToByteEncoder handler, ChannelHandlerContext ctx, T msg, ByteBuf out) throws Exception {
        handler.encode(ctx, msg, out);
        return out;
    }
}
