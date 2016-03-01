package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.transformers.OutgoingTransformer;

@ChannelHandler.Sharable
public class ViaOutboundHandler extends ChannelOutboundHandlerAdapter {
    private final OutgoingTransformer outgoingTransformer;
    private final ViaVersionInitializer init;

    public ViaOutboundHandler(Channel c, ConnectionInfo info, ViaVersionInitializer init) {
        this.init = init;
        this.outgoingTransformer = new OutgoingTransformer(c, info, init);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise channelPromise) throws Exception {
        try {
            if (channelPromise.isDone()) return; // don't break any <3s
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
                    outgoingTransformer.transform(id, bytebuf, newPacket);
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
            super.write(ctx, msg, channelPromise);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
