package us.myles.ViaVersion.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk;
import net.minecraft.server.v1_8_R3.World;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.Core;

public class ViaOutboundPacketHandler extends ChannelOutboundHandlerAdapter {
    private final ConnectionInfo info;

    public ViaOutboundPacketHandler(Channel c, ConnectionInfo info) {
        this.info = info;
    }
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
        if(o instanceof Packet){
            info.setLastPacket(o);
            /* This transformer is more for fixing issues which we find hard at byte level :) */
            if(o instanceof PacketPlayOutMapChunkBulk){
                PacketPlayOutMapChunkBulk bulk = (PacketPlayOutMapChunkBulk) o;
                int[] locX = Core.getPrivateField(bulk, "a", int[].class);
                int[] locZ = Core.getPrivateField(bulk, "b", int[].class);

                World world = Core.getPrivateField(bulk, "world", World.class);
                for(int i = 0;i<locX.length;i++){
                    int x = locX[i];
                    int z = locZ[i];
                    channelHandlerContext.write(new PacketPlayOutMapChunk(world.getChunkAt(x, z), true, 65535)); // magic was 65535
                }
                return;
            }
        }
        super.write(channelHandlerContext, o, channelPromise);
    }
}
