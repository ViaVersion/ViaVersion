package us.myles.ViaVersion.bungee.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;

import java.lang.reflect.Method;

public class ViaVersionInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelInitializer<Channel> original;
    private Method method;

    public ViaVersionInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
        try {
            this.method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public ChannelInitializer<Channel> getOriginal() {
        return original;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        UserConnection info = new UserConnection(socketChannel);
        // init protocol
        new ProtocolPipeline(info);
        // Add originals
        this.method.invoke(this.original, socketChannel);
        // Add our transformers
        ViaEncodeHandler encoder = new ViaEncodeHandler(info, (MinecraftEncoder) socketChannel.pipeline().get("packet-encoder"));
        ViaDecodeHandler decoder = new ViaDecodeHandler(info, (MinecraftDecoder) socketChannel.pipeline().get("packet-decoder"));
//        ViaPacketHandler chunkHandler = new ViaPacketHandler(info);

        socketChannel.pipeline().replace("packet-encoder", "packet-encoder", encoder);
        socketChannel.pipeline().replace("packet-decoder", "packet-decoder", decoder);
//        socketChannel.pipeline().addAfter("packet_handler", "viaversion_packet_handler", chunkHandler);
    }
}
