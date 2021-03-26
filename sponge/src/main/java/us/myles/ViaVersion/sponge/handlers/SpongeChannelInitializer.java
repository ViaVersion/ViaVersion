/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.myles.ViaVersion.sponge.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;

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
        if (Via.getAPI().getServerVersion().isKnown()
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
