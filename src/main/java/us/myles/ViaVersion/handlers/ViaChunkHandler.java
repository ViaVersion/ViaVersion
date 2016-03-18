package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.util.List;

public class ViaChunkHandler extends MessageToMessageEncoder {
    private final UserConnection info;

    public ViaChunkHandler(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, List list) throws Exception {
        // Split chunk bulk packet up in to single chunk packets before it reached the encoder.
        // This will prevent issues with several plugins and other protocol handlers due to the chunk being sent twice.
        // It also sends the chunk in the right order possible resolving some issues with added chunk/block/entity data.
        if (!(o instanceof ByteBuf)) {
            info.setLastPacket(o);
            /* This transformer is more for fixing issues which we find hard at packet level :) */
            if (o.getClass().getName().endsWith("PacketPlayOutMapChunkBulk") && info.isActive()) {
                list.addAll(info.get(ClientChunks.class).transformMapChunkBulk(o));
                return;
            }
        }

        list.add(o);
    }
}
