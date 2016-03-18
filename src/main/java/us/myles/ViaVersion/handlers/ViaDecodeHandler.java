package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ViaDecodeHandler extends ByteToMessageDecoder {

    private final ByteToMessageDecoder minecraftDecoder;
    private final UserConnection info;

    public ViaDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        // use transformers
        if (bytebuf.readableBytes() > 0) {
            if (info.isActive()) {
                int id = Type.VAR_INT.read(bytebuf);
                // Transform
                try {

                    PacketWrapper wrapper = new PacketWrapper(id, bytebuf, info);
                    ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                    protInfo.getPipeline().transform(Direction.INCOMING, protInfo.getState(), wrapper);
                    ByteBuf newPacket = ctx.alloc().buffer();
                    wrapper.writeToBuffer(newPacket);

                    bytebuf.clear();
                    bytebuf = newPacket;
                } catch (Exception e) {
                    bytebuf.clear();
                    throw e;
                }
            }
            // call minecraft decoder
            try {
                list.addAll(PipelineUtil.callDecode(this.minecraftDecoder, ctx, bytebuf));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
