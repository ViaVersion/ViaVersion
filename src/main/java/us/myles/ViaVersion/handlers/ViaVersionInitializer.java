package us.myles.ViaVersion.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.classgenerator.ClassGenerator;
import us.myles.ViaVersion.classgenerator.HandlerConstructor;

import java.lang.reflect.Method;

public class ViaVersionInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelInitializer<SocketChannel> original;
    private Method method;

    public ViaVersionInitializer(ChannelInitializer<SocketChannel> oldInit) {
        this.original = oldInit;
        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public ChannelInitializer<SocketChannel> getOriginal() {
        return original;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        UserConnection info = new UserConnection(socketChannel);
        // init protocol
        new ProtocolPipeline(info);
        // Add originals
        this.method.invoke(this.original, socketChannel);

        HandlerConstructor constructor = ClassGenerator.getConstructor();
        // Add our transformers
        MessageToByteEncoder encoder = constructor.newEncodeHandler(info, (MessageToByteEncoder) socketChannel.pipeline().get("encoder"));
        ByteToMessageDecoder decoder = constructor.newDecodeHandler(info, (ByteToMessageDecoder) socketChannel.pipeline().get("decoder"));
        ViaPacketHandler chunkHandler = new ViaPacketHandler(info);

        socketChannel.pipeline().replace("encoder", "encoder", encoder);
        socketChannel.pipeline().replace("decoder", "decoder", decoder);
        socketChannel.pipeline().addAfter("packet_handler", "viaversion_packet_handler", chunkHandler);
    }
}
