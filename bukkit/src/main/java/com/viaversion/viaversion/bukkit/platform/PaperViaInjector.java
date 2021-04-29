package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.bukkit.handlers.BukkitChannelInitializer;
import io.netty.channel.Channel;
import net.kyori.adventure.key.Key;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class PaperViaInjector {
    public static final boolean PAPER_INJECTION_METHOD = hasPaperInjectionMethod();
    public static final boolean PAPER_PROTOCOL_METHOD = hasServerProtocolMethod();

    private PaperViaInjector() {
    }

    public static void setPaperChannelInitializeListener() throws ReflectiveOperationException {
        // Call io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(net.kyori.adventure.key.Key, io.papermc.paper.network.ChannelInitializeListener)
        // Create an interface proxy of ChannelInitializeListener
        Class<?> listenerClass = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
        Object channelInitializeListener = Proxy.newProxyInstance(BukkitViaInjector.class.getClassLoader(), new Class[]{listenerClass}, (proxy, method, args) -> {
            if (method.getName().equals("afterInitChannel")) {
                BukkitChannelInitializer.afterChannelInitialize((Channel) args[0]);
                return null;
            }
            return method.invoke(proxy, args);
        });

        Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Method addListenerMethod = holderClass.getDeclaredMethod("addListener", Key.class, listenerClass);
        addListenerMethod.invoke(null, Key.key("viaversion", "injector"), channelInitializeListener);
    }

    private static boolean hasServerProtocolMethod() {
        try {
            Class.forName("org.bukkit.UnsafeValues").getDeclaredMethod("getProtocolVersion");
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static boolean hasPaperInjectionMethod() {
        try {
            Class.forName("io.papermc.paper.network.ChannelInitializeListener");
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
