package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.transformers.IncomingTransformer;

@ChannelHandler.Sharable
public class ViaInboundHandler extends ChannelInboundHandlerAdapter {
    private final IncomingTransformer incomingTransformer;
    private final SocketChannel socketChannel;

    public ViaInboundHandler(SocketChannel c, ConnectionInfo info) {
        this.socketChannel = c;
        this.incomingTransformer = new IncomingTransformer(this, c, info);
    }
    
    public void remove(){
        socketChannel.pipeline().remove("via_incoming");
        socketChannel.pipeline().remove("via_outgoing");
        socketChannel.pipeline().remove("via_outgoing2");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean compression = ctx.pipeline().get("compress") != null;

        if (msg instanceof ByteBuf) {
            ByteBuf bytebuf = (ByteBuf) msg;
            if (compression) {
                // decompress :)
                bytebuf = PacketUtil.decompress(ctx, bytebuf);
            }
            int id = PacketUtil.readVarInt(bytebuf);
            // Transform
            ByteBuf newPacket = ctx.alloc().buffer();
            try {
                incomingTransformer.transform(id, bytebuf, newPacket);
            } catch (CancelException e) {
                return;
            } finally {
                bytebuf.release();
            }
            if (compression) {
                // recompress :)
                newPacket = PacketUtil.compress(ctx, newPacket);
            }
            msg = newPacket;
        }
        super.channelRead(ctx, msg);
    }


}
