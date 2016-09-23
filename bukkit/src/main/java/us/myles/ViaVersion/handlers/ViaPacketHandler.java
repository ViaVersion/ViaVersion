package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.List;

public class ViaPacketHandler extends MessageToMessageEncoder {
    private final UserConnection info;

    public ViaPacketHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, List list) throws Exception {
        // Split chunks bulk packet up in to single chunks packets before it reached the encoder.
        // This will prevent issues with several plugins and other protocol handlers due to the chunks being sent twice.
        // It also sends the chunks in the right order possible resolving some issues with added chunks/block/entity data.
        if (!(o instanceof ByteBuf)) {
            info.setLastPacket(o);
            /* This transformer is more for fixing issues which we find hard at packet level :) */
            if (info.isActive()) {
                if (info.get(ProtocolInfo.class).getPipeline().filter(o, list)) {
                    return;
                }
            }
        }

        list.add(o);
    }
}
