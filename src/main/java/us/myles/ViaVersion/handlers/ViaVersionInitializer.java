package us.myles.ViaVersion.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import us.myles.ViaVersion.ConnectionInfo;

import java.lang.reflect.Method;

public class ViaVersionInitializer extends ChannelInitializer<SocketChannel> {
	
	// shared by all connections
    private final ChannelInitializer<SocketChannel> oldInit;
    private Method method;

    public ViaVersionInitializer(ChannelInitializer<SocketChannel> oldInit) {
        this.oldInit = oldInit;
        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
    	ConnectionInfo info = new ConnectionInfo();
        // Add originals
        this.method.invoke(this.oldInit, socketChannel);
        // Add our transformers
        
        ViaInboundHandler inbound = new ViaInboundHandler(socketChannel, info);
        ViaOutboundHandler outbound = new ViaOutboundHandler(socketChannel, info, this);
        ViaOutboundPacketHandler outbound2 = new ViaOutboundPacketHandler(socketChannel, info);
        socketChannel.pipeline().addBefore("decoder", "via_incoming", inbound);
        socketChannel.pipeline().addBefore("packet_handler", "via_outgoing2", outbound2);
        socketChannel.pipeline().addBefore("encoder", "via_outgoing", outbound);

    }

}
