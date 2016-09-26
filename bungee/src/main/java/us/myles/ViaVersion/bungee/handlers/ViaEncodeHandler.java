package us.myles.ViaVersion.bungee.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftEncoder;
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

public class ViaEncodeHandler extends MinecraftEncoder {
    private final UserConnection info;
    private final MinecraftEncoder minecraftEncoder;

    public ViaEncodeHandler(UserConnection info, MinecraftEncoder minecraftEncoder) {
        super(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion());
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
    }


    @Override
    protected void encode(final ChannelHandlerContext ctx, DefinedPacket o, final ByteBuf bytebuf) throws Exception {
        // call minecraft encoder
        try {
            BungeePipelineUtil.callEncode(this.minecraftEncoder, ctx, o, bytebuf);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
        }
        if (bytebuf.readableBytes() == 0) {
            throw new CancelException();
        }
        // Increment sent
        info.incrementSent();
        if (info.isActive()) {
            // Handle ID
            int id = Type.VAR_INT.read(bytebuf);
            // Transform
            ByteBuf oldPacket = bytebuf.copy();
            bytebuf.clear();

            try {
                PacketWrapper wrapper = new PacketWrapper(id, oldPacket, info);
                ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                protInfo.getPipeline().transform(Direction.OUTGOING, protInfo.getState(), wrapper);
                wrapper.writeToBuffer(bytebuf);
            } catch (Exception e) {
                bytebuf.clear();
                throw e;
            } finally {
                oldPacket.release();
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
        this.minecraftEncoder.setProtocol(protocol);
    }

    @Override
    public void setProtocolVersion(int protocolVersion) {
        this.minecraftEncoder.setProtocolVersion(protocolVersion);
    }
}
