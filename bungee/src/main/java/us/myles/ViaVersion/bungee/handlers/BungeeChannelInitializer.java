package us.myles.ViaVersion.bungee.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.NettyChannelInternals;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;

import java.lang.reflect.Method;

public class BungeeChannelInitializer extends ChannelInitializer<Channel> {
    @Getter
    private final ChannelInitializer<Channel> original;

    public BungeeChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Override
    protected void initChannel(Channel socketChannel) throws Exception {
        UserConnection info = new UserConnection(socketChannel);
        // init protocol
        new ProtocolPipeline(info);
        // Add originals
        NettyChannelInternals.initChannel(this.original, socketChannel);

        if (socketChannel.pipeline().get("packet-encoder") == null) return; // Don't inject if no packet-encoder
        if (socketChannel.pipeline().get("packet-decoder") == null) return; // Don't inject if no packet-decoder
        // Add our transformers
        BungeeEncodeHandler encoder = new BungeeEncodeHandler(info);
        BungeeDecodeHandler decoder = new BungeeDecodeHandler(info);

        socketChannel.pipeline().addBefore("packet-encoder", "via-encoder", encoder);
        socketChannel.pipeline().addBefore("packet-decoder", "via-decoder", decoder);

    }
}
