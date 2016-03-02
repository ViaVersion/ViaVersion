package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.transformers.IncomingTransformer;
import us.myles.ViaVersion.util.PacketUtil;

import java.nio.channels.ClosedChannelException;
import java.util.List;

public class ViaDecodeHandler extends ByteToMessageDecoder {
    private final IncomingTransformer incomingTransformer;
    private final ByteToMessageDecoder minecraftDecoder;
    private final ConnectionInfo info;

    public ViaDecodeHandler(ConnectionInfo info, ByteToMessageDecoder minecraftDecoder) {
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
        this.incomingTransformer = new IncomingTransformer(info);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        // use transformers
        if (bytebuf.readableBytes() > 0) {
            if (info.isActive()) {
                int id = PacketUtil.readVarInt(bytebuf);
                // Transform
                ByteBuf newPacket = ctx.alloc().buffer();
                try {
                    incomingTransformer.transform(id, bytebuf, newPacket);
                    bytebuf = newPacket;
                } catch (CancelException e) {
                    bytebuf.readBytes(bytebuf.readableBytes());
                    throw e;
                }
            }
            // call minecraft decoder
            list.addAll(PacketUtil.callDecode(this.minecraftDecoder, ctx, bytebuf));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause.getCause().getCause() instanceof CancelException)
                && !(cause.getCause().getCause() instanceof ClosedChannelException)) {
            if (!(cause.getCause() instanceof CancelException)
                    && !(cause.getCause() instanceof ClosedChannelException)) {
                if (!(cause instanceof CancelException)
                        && !(cause instanceof ClosedChannelException)) {
                    if (cause instanceof Exception)
                        throw (Exception) cause;
                }
            }
        }
    }

}
