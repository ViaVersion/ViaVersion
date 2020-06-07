package us.myles.ViaVersion.bungee.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bungee.util.BungeePipelineUtil;
import us.myles.ViaVersion.exception.CancelEncoderException;

import java.util.List;

@ChannelHandler.Sharable
public class BungeeEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression;

    public BungeeEncodeHandler(UserConnection info) {
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

    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf draft) {
        boolean needsCompress = false;
        if (!handledCompression) {
            if (ctx.pipeline().names().indexOf("compress") > ctx.pipeline().names().indexOf("via-encoder")) {
                // Need to decompress this packet due to bad order
                ByteBuf decompressed = BungeePipelineUtil.decompress(ctx, draft);
                try {
                    draft.clear().writeBytes(decompressed);
                } finally {
                    decompressed.release();
                }
                ChannelHandler dec = ctx.pipeline().get("via-decoder");
                ChannelHandler enc = ctx.pipeline().get("via-encoder");
                ctx.pipeline().remove(dec);
                ctx.pipeline().remove(enc);
                ctx.pipeline().addAfter("decompress", "via-decoder", dec);
                ctx.pipeline().addAfter("compress", "via-encoder", enc);
                needsCompress = true;
                handledCompression = true;
            }
        }
        return needsCompress;
    }

    private void recompress(ChannelHandlerContext ctx, ByteBuf draft) {
        ByteBuf compressed = BungeePipelineUtil.compress(ctx, draft);
        try {
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
