package us.myles.ViaVersion.sponge.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.exception.CancelDecoderException;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SpongeDecodeHandler extends ByteToMessageDecoder {

    private final ByteToMessageDecoder minecraftDecoder;
    private final UserConnection info;

    public SpongeDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        if (!info.checkIncomingPacket()) {
            bytebuf.clear(); // Don't accumulate
            throw CancelDecoderException.generate(null);
        }

        ByteBuf draft = null;
        try {
            if (info.shouldTransformPacket()) {
                draft = ctx.alloc().buffer().writeBytes(bytebuf);
                info.transformIncoming(draft, CancelDecoderException::generate);
            }

            try {
                list.addAll(PipelineUtil.callDecode(this.minecraftDecoder, ctx, draft == null ? bytebuf : draft));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        } finally {
            if (draft != null) {
                draft.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelDecoderException) return;
        super.exceptionCaught(ctx, cause);
    }
}
