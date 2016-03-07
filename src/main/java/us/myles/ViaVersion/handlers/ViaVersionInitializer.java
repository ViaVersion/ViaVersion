package us.myles.ViaVersion.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.ConnectionInfo;

import java.lang.reflect.Method;

public class ViaVersionInitializer extends ChannelInitializer<SocketChannel> {

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
        ConnectionInfo info = new ConnectionInfo(socketChannel);
        // Add originals
        this.method.invoke(this.oldInit, socketChannel);
        // Add our transformers
        ViaEncodeHandler encoder = new ViaEncodeHandler(info, (MessageToByteEncoder) socketChannel.pipeline().get("encoder"));
        ViaDecodeHandler decoder = new ViaDecodeHandler(info, (ByteToMessageDecoder) socketChannel.pipeline().get("decoder"));

        socketChannel.pipeline().replace("encoder", "encoder", encoder);
        socketChannel.pipeline().replace("decoder", "decoder", decoder);
    }
}
