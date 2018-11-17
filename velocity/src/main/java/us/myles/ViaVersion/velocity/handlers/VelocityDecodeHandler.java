package us.myles.ViaVersion.velocity.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.AllArgsConstructor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;

import java.util.List;

@ChannelHandler.Sharable
@AllArgsConstructor
public class VelocityDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection info;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        // use transformers
        if (bytebuf.readableBytes() > 0) {
            // Ignore if pending disconnect
            if (info.isPendingDisconnect()) {
                return;
            }
            // Increment received
            boolean second = info.incrementReceived();
            // Check PPS
            if (second) {
                if (info.handlePPS())
                    return;
            }
            info.getVelocityLock().readLock().lock();
            if (info.isActive()) {
                // Handle ID
                int id = Type.VAR_INT.read(bytebuf);
                // Transform
                ByteBuf newPacket = ctx.alloc().buffer();
                try {
                    if (id == PacketWrapper.PASSTHROUGH_ID) {
                        newPacket.writeBytes(bytebuf);
                    } else {
                        PacketWrapper wrapper = new PacketWrapper(id, bytebuf, info);
                        ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                        protInfo.getPipeline().transform(Direction.INCOMING, protInfo.getState(), wrapper);
                        wrapper.writeToBuffer(newPacket);
                    }

                    bytebuf.clear();
                    bytebuf = newPacket;
                } catch (Exception e) {
                    // Clear Buffer
                    bytebuf.clear();
                    // Release Packet, be free!
                    newPacket.release();
                    info.getVelocityLock().readLock().unlock();
                    throw e;
                }
            } else {
                bytebuf.retain();
            }
            info.getVelocityLock().readLock().unlock();

            out.add(bytebuf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
