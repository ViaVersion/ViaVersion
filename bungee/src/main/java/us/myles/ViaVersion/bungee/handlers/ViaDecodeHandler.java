package us.myles.ViaVersion.bungee.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.Protocol;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.bungee.util.BungeePipelineUtil;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ViaDecodeHandler extends MinecraftDecoder {

    private final MinecraftDecoder minecraftDecoder;
    private final UserConnection info;

    public ViaDecodeHandler(UserConnection info, MinecraftDecoder minecraftDecoder) {
        super(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion());
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
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
                    throw e;
                }
            }

            // call minecraft decoder
            try {
                list.addAll(BungeePipelineUtil.callDecode(this.minecraftDecoder, ctx, bytebuf));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                }
            } finally {
                if (info.isActive()) {
                    bytebuf.release();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void setProtocol(Protocol protocol) {
        this.minecraftDecoder.setProtocol(protocol);
    }

    @Override
    public void setProtocolVersion(int protocolVersion) {
        this.minecraftDecoder.setProtocolVersion(protocolVersion);
    }
}
