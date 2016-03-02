package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.transformers.OutgoingTransformer;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.nio.channels.ClosedChannelException;

public class ViaEncodeHandler extends MessageToByteEncoder {
    private final ConnectionInfo info;
    private final MessageToByteEncoder minecraftEncoder;
    private final OutgoingTransformer outgoingTransformer;

    public ViaEncodeHandler(ConnectionInfo info, MessageToByteEncoder minecraftEncoder) {
        this.info = info;
        this.minecraftEncoder = minecraftEncoder;
        this.outgoingTransformer = new OutgoingTransformer(info);
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, ByteBuf bytebuf) throws Exception {
        // handle the packet type
        if (!(o instanceof ByteBuf)) {
            info.setLastPacket(o);
            /* This transformer is more for fixing issues which we find hard at packet level :) */
            if (o.getClass().getName().endsWith("PacketPlayOutMapChunkBulk") && info.isActive()) {
                int[] locX = ReflectionUtil.get(o, "a", int[].class);
                int[] locZ = ReflectionUtil.get(o, "b", int[].class);

                Object world = ReflectionUtil.get(o, "world", ReflectionUtil.nms("World"));
                Class<?> mapChunk = ReflectionUtil.nms("PacketPlayOutMapChunk");
                Constructor constructor = mapChunk.getDeclaredConstructor(ReflectionUtil.nms("Chunk"), boolean.class, int.class);
                for (int i = 0; i < locX.length; i++) {
                    int x = locX[i];
                    int z = locZ[i];
                    // world invoke function
                    Object chunk = ReflectionUtil.nms("World").getDeclaredMethod("getChunkAt", int.class, int.class).invoke(world, x, z);
                    Object packet = constructor.newInstance(chunk, true, 65535);
                    ctx.pipeline().writeAndFlush(packet);
                }
                bytebuf.readBytes(bytebuf.readableBytes());
                throw new CancelException();
            }
            // call minecraft encoder
            PacketUtil.callEncode(this.minecraftEncoder, ctx, o, bytebuf);
        }
        if (bytebuf.readableBytes() == 0) {
            throw new CancelException();
        }
        if (info.isActive()) {
            int id = PacketUtil.readVarInt(bytebuf);
            // Transform
            ByteBuf oldPacket = bytebuf.copy();
            bytebuf.clear();
            try {
                outgoingTransformer.transform(id, oldPacket, bytebuf);
            } catch (CancelException e) {
                bytebuf.readBytes(bytebuf.readableBytes());
                throw e;
            } finally {
                oldPacket.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause.getCause().getCause() instanceof CancelException)
                && !(cause.getCause().getCause() instanceof ClosedChannelException)) {
            if (!(cause.getCause() instanceof CancelException)
                    && !(cause.getCause() instanceof ClosedChannelException)) {
                if (!(cause instanceof CancelException)
                        && !(cause instanceof ClosedChannelException)) {
                    if (cause instanceof Exception)
                        throw (Exception) cause;
                }
            }
        }
    }
}
