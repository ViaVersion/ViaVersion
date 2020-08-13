package us.myles.ViaVersion.sponge.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.lang.reflect.Method;

public class SpongeChannelInitializer extends ChannelInitializer<Channel> {

    private final ChannelInitializer<Channel> original;
    private Method method;

    public SpongeChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Ensure ViaVersion is loaded
        if (ProtocolRegistry.SERVER_PROTOCOL != -1
                && channel instanceof SocketChannel) { // channel can be LocalChannel on internal server
            UserConnection info = new UserConnection((SocketChannel) channel);
            // init protocol
            new ProtocolPipeline(info);
            // Add originals
            this.method.invoke(this.original, channel);
            // Add our transformers
            MessageToByteEncoder encoder = new SpongeEncodeHandler(info, (MessageToByteEncoder) channel.pipeline().get("encoder"));
            ByteToMessageDecoder decoder = new SpongeDecodeHandler(info, (ByteToMessageDecoder) channel.pipeline().get("decoder"));
            SpongePacketHandler chunkHandler = new SpongePacketHandler(info);

            channel.pipeline().replace("encoder", "encoder", encoder);
            channel.pipeline().replace("decoder", "decoder", decoder);
            channel.pipeline().addAfter("packet_handler", "viaversion_packet_handler", chunkHandler);
        } else {
            this.method.invoke(this.original, channel);
        }
    }

    public ChannelInitializer<Channel> getOriginal() {
        return original;
    }
}
