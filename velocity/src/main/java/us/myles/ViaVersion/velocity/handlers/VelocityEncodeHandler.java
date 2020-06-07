package us.myles.ViaVersion.velocity.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.exception.CancelEncoderException;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
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
        info.checkOutgoingPacket();
        if (!info.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf draft = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            boolean needsCompress = handleCompressionOrder(ctx, draft);

            info.transformOutgoing(draft, CancelEncoderException::generate);

            if (needsCompress) {
                recompress(ctx, draft);
            }
            out.add(draft.retain());
        } finally {
            draft.release();
        }
    }

    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf draft) throws InvocationTargetException {
        boolean needsCompress = false;
        if (!handledCompression
                && ctx.pipeline().names().indexOf("compression-encoder") > ctx.pipeline().names().indexOf("via-encoder")) {
            // Need to decompress this packet due to bad order
            ByteBuf decompressed = (ByteBuf) PipelineUtil.callDecode((MessageToMessageDecoder<?>) ctx.pipeline().get("compression-decoder"), ctx, draft).get(0);
            try {
                draft.clear().writeBytes(decompressed);
            } finally {
                decompressed.release();
            }
            ChannelHandler encoder = ctx.pipeline().get("via-encoder");
            ChannelHandler decoder = ctx.pipeline().get("via-decoder");
            ctx.pipeline().remove(encoder);
            ctx.pipeline().remove(decoder);
            ctx.pipeline().addAfter("compression-encoder", "via-encoder", encoder);
            ctx.pipeline().addAfter("compression-decoder", "via-decoder", decoder);
            needsCompress = true;
            handledCompression = true;
        }
        return needsCompress;
    }

    private void recompress(ChannelHandlerContext ctx, ByteBuf draft) throws InvocationTargetException {
        ByteBuf compressed = ctx.alloc().buffer();
        try {
            PipelineUtil.callEncode((MessageToByteEncoder<?>) ctx.pipeline().get("compression-encoder"), ctx, draft, compressed);
            draft.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelEncoderException) return;
        super.exceptionCaught(ctx, cause);
    }
}
