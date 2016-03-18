package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.base.ProtocolInfo;

import java.lang.reflect.InvocationTargetException;

public class ViaEncodeHandler extends MessageToByteEncoder {
    private final UserConnection info;
    private final MessageToByteEncoder minecraftEncoder;

    public ViaEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
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
                PacketWrapper wrapper = new PacketWrapper(id, oldPacket, info);
                ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                protInfo.getPipeline().transform(Direction.OUTGOING, protInfo.getState(), wrapper);
                wrapper.writeToBuffer(bytebuf);
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
