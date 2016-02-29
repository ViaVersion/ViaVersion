package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.PacketUtil;
import us.myles.ViaVersion.transformers.IncomingTransformer;

@ChannelHandler.Sharable
public class ViaInboundHandler extends ChannelInboundHandlerAdapter {
    private final IncomingTransformer incomingTransformer;
    private final ViaVersionInitializer init;

    public ViaInboundHandler(Channel c, ConnectionInfo info, ViaVersionInitializer init) {
        this.init = init;
        this.incomingTransformer = new IncomingTransformer(c, info, init);
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
