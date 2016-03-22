package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.transformers.OutgoingTransformer;
import us.myles.ViaVersion.util.PacketUtil;

import java.lang.reflect.InvocationTargetException;

public class ViaEncodeHandler extends MessageToByteEncoder {
    private final ConnectionInfo info;
    private final MessageToByteEncoder minecraftEncoder;
    private final OutgoingTransformer outgoingTransformer;

    public ViaEncodeHandler(ConnectionInfo info, MessageToByteEncoder minecraftEncoder) {
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
        this.outgoingTransformer = new OutgoingTransformer(info);
    }


    @Override
    protected void encode(final ChannelHandlerContext ctx, Object o, final ByteBuf bytebuf) throws Exception {
        // handle the packet type
        if (!(o instanceof ByteBuf)) {
            // call minecraft encoder
            try {
                PacketUtil.callEncode(this.minecraftEncoder, ctx, o, bytebuf);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                }
            }
        }
        if (bytebuf.readableBytes() == 0) {
            throw new CancelException();
        }
        if (info.isActive()) {
            int id = PacketUtil.readVarInt(bytebuf);
            // Transform
            ByteBuf oldPacket = bytebuf.copy();
            bytebuf.clear();
            try {
                outgoingTransformer.transform(id, oldPacket, bytebuf);
            } catch (Exception e) {
                bytebuf.clear();
                throw e;
            } finally {
                oldPacket.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PacketUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
