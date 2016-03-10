package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ViaChunkHandler extends MessageToMessageEncoder {
    private final ConnectionInfo info;

    public ViaChunkHandler(ConnectionInfo info) {
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
            if(o.getClass().getName().endsWith("PacketPlayOutMapChunkBulk") && info.isActive()) {
                final int[] locX = ReflectionUtil.get(o, "a", int[].class);
                final int[] locZ = ReflectionUtil.get(o, "b", int[].class);
                final Object world = ReflectionUtil.get(o, "world", ReflectionUtil.nms("World"));
                Class<?> mapChunk = ReflectionUtil.nms("PacketPlayOutMapChunk");
                final Constructor constructor = mapChunk.getDeclaredConstructor(ReflectionUtil.nms("Chunk"), boolean.class, int.class);
                for(int i = 0; i < locX.length; i++) {
                    int x = locX[i];
                    int z = locZ[i];
                    // world invoke function
                    try {
                        Object chunk = ReflectionUtil.nms("World").getDeclaredMethod("getChunkAt", int.class, int.class).invoke(world, x, z);
                        Object packet = constructor.newInstance(chunk, true, 65535);
                        list.add(packet);
                    } catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

                return;
            }
        }

        list.add(o);
    }
}
