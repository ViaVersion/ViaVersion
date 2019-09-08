package io.netty.channel;

public final class NettyChannelInternals {

    private NettyChannelInternals() { }

    public static <C extends Channel> void initChannel(ChannelInitializer<C> initializer, C ch) throws Exception {
        initializer.initChannel(ch);
    }
}
