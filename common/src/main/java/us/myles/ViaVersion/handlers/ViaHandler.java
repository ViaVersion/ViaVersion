package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface ViaHandler {
    void transform(ByteBuf bytebuf) throws Exception;

    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

}
