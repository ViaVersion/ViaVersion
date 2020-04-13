package us.myles.ViaVersion.velocity.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;

import java.util.List;

@ChannelHandler.Sharable
public class VelocityEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression;

    public VelocityEncodeHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (bytebuf.readableBytes() == 0) {
            throw Via.getManager().isDebug() ? new CancelException() : CancelException.CACHED;
        }
        boolean needsCompress = false;
        if (!handledCompression
                && ctx.pipeline().names().indexOf("compression-encoder") > ctx.pipeline().names().indexOf("via-encoder")) {
            // Need to decompress this packet due to bad order
            bytebuf = (ByteBuf) PipelineUtil.callDecode((MessageToMessageDecoder) ctx.pipeline().get("compression-decoder"), ctx, bytebuf).get(0);
            ChannelHandler encoder = ctx.pipeline().get("via-encoder");
            ChannelHandler decoder = ctx.pipeline().get("via-decoder");
            ctx.pipeline().remove(encoder);
            ctx.pipeline().remove(decoder);
            ctx.pipeline().addAfter("compression-encoder", "via-encoder", encoder);
            ctx.pipeline().addAfter("compression-decoder", "via-decoder", decoder);
            needsCompress = true;
            handledCompression = true;
        } else {
            bytebuf.retain();
        }
        // Increment sent
        info.incrementSent();


        if (info.isActive()) {
            // Handle ID
            int id = Type.VAR_INT.read(bytebuf);
            // Transform
            ByteBuf newPacket = bytebuf.alloc().buffer();
            try {
                PacketWrapper wrapper = new PacketWrapper(id, bytebuf, info);
                ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                protInfo.getPipeline().transform(Direction.OUTGOING, protInfo.getState(), wrapper);

                wrapper.writeToBuffer(newPacket);

                bytebuf.clear();
                bytebuf.release();
                bytebuf = newPacket;
            } catch (Throwable e) {
                bytebuf.clear();
                bytebuf.release();
                newPacket.release();
                throw e;
            }
        }

        if (needsCompress) {
            ByteBuf old = bytebuf;
            bytebuf = ctx.alloc().buffer();
            try {
                PipelineUtil.callEncode((MessageToByteEncoder) ctx.pipeline().get("compression-encoder"), ctx, old, bytebuf);
            } finally {
                old.release();
            }
        }
        out.add(bytebuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
