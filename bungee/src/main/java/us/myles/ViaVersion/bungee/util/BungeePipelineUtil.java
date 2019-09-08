package us.myles.ViaVersion.bungee.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.NettyCodecInternals;

import java.util.ArrayList;
import java.util.List;

public final class BungeePipelineUtil {

    private BungeePipelineUtil() { }

    public static List<Object> callDecode(MessageToMessageDecoder decoder, ChannelHandlerContext ctx, ByteBuf input) throws Exception {
        return NettyCodecInternals.callDecode(decoder, ctx, input, new ArrayList<>());
    }

    public static ByteBuf callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, ByteBuf input) throws Exception {
        return NettyCodecInternals.callEncode(encoder, ctx, input, ctx.alloc().buffer());
    }

    public static ByteBuf decompress(ChannelHandlerContext ctx, ByteBuf bytebuf) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        return (ByteBuf) callDecode((MessageToMessageDecoder) pipeline.get("decompress"), pipeline.context("decompress"), bytebuf).get(0);
    }

    public static ByteBuf compress(ChannelHandlerContext ctx, ByteBuf bytebuf) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        return callEncode((MessageToByteEncoder) pipeline.get("compress"), pipeline.context("compress"), bytebuf);
    }
}
