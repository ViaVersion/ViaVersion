package us.myles.ViaVersion.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import us.myles.ViaVersion.ConnectionInfo;

import java.lang.reflect.Method;

public class ViaVersionInitializer extends ChannelInitializer<SocketChannel> {
    private final ChannelInitializer<SocketChannel> oldInit;
    private Method method;
    private ConnectionInfo info;
    private ViaInboundHandler inbound;
    private ViaOutboundHandler outbound;
    private SocketChannel socketChannel;
    private ViaOutboundPacketHandler outbound2;

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
        info = new ConnectionInfo();
        // Add originals
        this.method.invoke(this.oldInit, socketChannel);
        // Add our transformers
        this.socketChannel = socketChannel;
        this.inbound = new ViaInboundHandler(socketChannel, info, this);
        this.outbound = new ViaOutboundHandler(socketChannel, info, this);
        this.outbound2 = new ViaOutboundPacketHandler(socketChannel, info);
        socketChannel.pipeline().addBefore("decoder", "via_incoming", this.inbound);
        socketChannel.pipeline().addBefore("packet_handler", "via_outgoing2", this.outbound2);
        socketChannel.pipeline().addBefore("encoder", "via_outgoing", this.outbound);

    }

    public void remove(){
        socketChannel.pipeline().remove("via_incoming");
        socketChannel.pipeline().remove("via_outgoing");
        socketChannel.pipeline().remove("via_outgoing2");
    }

}
