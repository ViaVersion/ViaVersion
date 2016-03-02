package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Constructor;

@ChannelHandler.Sharable
public class ViaOutboundPacketHandler extends ChannelOutboundHandlerAdapter {
    private final ConnectionInfo info;

    public ViaOutboundPacketHandler(ConnectionInfo info) {
        this.info = info;
    }

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
        if (!(o instanceof ByteBuf)) {
            info.setLastPacket(o);
            /* This transformer is more for fixing issues which we find hard at byte level :) */
            if (o.getClass().getName().endsWith("PacketPlayOutMapChunkBulk")) {
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
                    channelHandlerContext.write(packet);
                }
                return;
            }
        }
        super.write(channelHandlerContext, o, channelPromise);
    }
}
