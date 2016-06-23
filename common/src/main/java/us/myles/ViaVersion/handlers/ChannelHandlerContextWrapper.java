package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

import java.net.SocketAddress;

public class ChannelHandlerContextWrapper implements ChannelHandlerContext {
    private ChannelHandlerContext base;
    private ViaHandler handler;

    public ChannelHandlerContextWrapper(ChannelHandlerContext base, ViaHandler handler) {
        this.base = base;
        this.handler = handler;
    }

    @Override
    public Channel channel() {
        return base.channel();
    }

    @Override
    public EventExecutor executor() {
        return base.executor();
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public ChannelHandler handler() {
        return base.handler();
    }

    @Override
    public boolean isRemoved() {
        return base.isRemoved();
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        base.fireChannelRegistered();
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        base.fireChannelUnregistered();
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        base.fireChannelActive();
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        base.fireChannelInactive();
        return this;
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable throwable) {
        base.fireExceptionCaught(throwable);
        return this;
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object o) {
        base.fireUserEventTriggered(o);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object o) {
        base.fireChannelRead(o);
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        base.fireChannelReadComplete();
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        base.fireChannelWritabilityChanged();
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        return base.bind(socketAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress) {
        return base.connect(socketAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
        return base.connect(socketAddress, socketAddress1);
    }

    @Override
    public ChannelFuture disconnect() {
        return base.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return base.close();
    }

    @Override
    public ChannelFuture deregister() {
        return base.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return base.bind(socketAddress, channelPromise);
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return base.connect(socketAddress, channelPromise);
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
        return base.connect(socketAddress, socketAddress1, channelPromise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise) {
        return base.disconnect(channelPromise);
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise) {
        return base.close(channelPromise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise) {
        return base.deregister(channelPromise);
    }

    @Override
    public ChannelHandlerContext read() {
        base.read();
        return this;
    }

    @Override
    public ChannelFuture write(Object o) {
        if (o instanceof ByteBuf) {
            if (transform((ByteBuf) o)) return base.newFailedFuture(new Throwable());
        }
        return base.write(o);
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise) {
        if (o instanceof ByteBuf) {
            if (transform((ByteBuf) o)) return base.newFailedFuture(new Throwable());
        }
        return base.write(o, channelPromise);
    }

    public boolean transform(ByteBuf buf) {
        try {
            handler.transform(buf);
            return false;
        } catch (Exception e) {
            try {
                handler.exceptionCaught(base, e);
            } catch (Exception e1) {
                base.fireExceptionCaught(e1);
            }
            return true;
        }
    }

    @Override
    public ChannelHandlerContext flush() {
        base.flush();
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
        ChannelFuture future = write(o, channelPromise);
        flush();
        return future;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        ChannelFuture future = write(o);
        flush();
        return future;
    }

    @Override
    public ChannelPipeline pipeline() {
        return base.pipeline();
    }

    @Override
    public ByteBufAllocator alloc() {
        return base.alloc();
    }

    @Override
    public ChannelPromise newPromise() {
        return base.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return base.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return base.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        return base.newFailedFuture(throwable);
    }

    @Override
    public ChannelPromise voidPromise() {
        return base.voidPromise();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
        return base.attr(attributeKey);
    }
}
