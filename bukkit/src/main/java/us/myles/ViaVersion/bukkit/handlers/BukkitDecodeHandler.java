package us.myles.ViaVersion.bukkit.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bukkit.util.NMSUtil;
import us.myles.ViaVersion.exception.CancelCodecException;
import us.myles.ViaVersion.exception.CancelDecoderException;
import us.myles.ViaVersion.exception.InformativeException;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BukkitDecodeHandler extends ByteToMessageDecoder {
    private final ByteToMessageDecoder minecraftDecoder;
    private final UserConnection info;

    public BukkitDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        if (!info.checkIncomingPacket()) {
            bytebuf.clear(); // Don't accumulate
            throw CancelDecoderException.generate(null);
        }

        ByteBuf transformedBuf = null;
        try {
            if (info.shouldTransformPacket()) {
                transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
                info.transformIncoming(transformedBuf, CancelDecoderException::generate);
            }

            try {
                list.addAll(PipelineUtil.callDecode(this.minecraftDecoder, ctx, transformedBuf == null ? bytebuf : transformedBuf));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        } finally {
            if (transformedBuf != null) {
                transformedBuf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) return; // ProtocolLib compat

        super.exceptionCaught(ctx, cause);
        if (!NMSUtil.isDebugPropertySet() && PipelineUtil.containsCause(cause, InformativeException.class)) {
            cause.printStackTrace(); // Print if CB doesn't already do it
        }
    }
}
