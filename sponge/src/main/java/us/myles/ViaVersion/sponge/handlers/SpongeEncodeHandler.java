package us.myles.ViaVersion.sponge.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.exception.CancelEncoderException;
import us.myles.ViaVersion.exception.CancelCodecException;
import us.myles.ViaVersion.handlers.ChannelHandlerContextWrapper;
import us.myles.ViaVersion.handlers.ViaHandler;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;

public class SpongeEncodeHandler extends MessageToByteEncoder<Object> implements ViaHandler {
    private final UserConnection info;
    private final MessageToByteEncoder<?> minecraftEncoder;

    public SpongeEncodeHandler(UserConnection info, MessageToByteEncoder<?> minecraftEncoder) {
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, Object o, final ByteBuf bytebuf) throws Exception {
        // handle the packet type
        if (!(o instanceof ByteBuf)) {
            // call minecraft encoder
            try {
                PipelineUtil.callEncode(this.minecraftEncoder, new ChannelHandlerContextWrapper(ctx, this), o, bytebuf);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        } else {
            bytebuf.writeBytes((ByteBuf) o);
        }
        transform(bytebuf);
    }

    @Override
    public void transform(ByteBuf bytebuf) throws Exception {
        if (!info.checkOutgoingPacket()) throw CancelEncoderException.generate(null);
        if (!info.shouldTransformPacket()) return;
        info.transformOutgoing(bytebuf, CancelEncoderException::generate);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelCodecException) return;
        super.exceptionCaught(ctx, cause);
    }
}
