/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.bukkit.handlers.BukkitChannelInitializer;
import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;

public final class PaperViaInjector {
    public static final boolean PAPER_INJECTION_METHOD = hasPaperInjectionMethod();
    public static final boolean PAPER_PROTOCOL_METHOD = hasServerProtocolMethod();
    public static final boolean PAPER_IS_STOPPING_METHOD = hasIsStoppingMethod();

    private PaperViaInjector() {
    }

    public static int getServerProtocolVersion() {
        if (!PaperViaInjector.PAPER_PROTOCOL_METHOD) {
            throw new UnsupportedOperationException("Paper method not available");
        }
        //noinspection deprecation
        return Bukkit.getUnsafe().getProtocolVersion();
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

    public static void removePaperChannelInitializeListener() throws ReflectiveOperationException {
        Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Method addListenerMethod = holderClass.getDeclaredMethod("removeListener", Key.class);
        addListenerMethod.invoke(null, Key.key("viaversion", "injector"));
    }

    private static boolean hasServerProtocolMethod() {
        return hasMethod("org.bukkit.UnsafeValues", "getProtocolVersion");
    }

    private static boolean hasPaperInjectionMethod() {
        return hasClass("io.papermc.paper.network.ChannelInitializeListener");
    }

    private static boolean hasIsStoppingMethod() {
        return hasMethod(Bukkit.class, "isStopping");
    }

    public static boolean hasClass(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasMethod(final String className, final String method) {
        try {
            Class.forName(className).getDeclaredMethod(method);
            return true;
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasMethod(final Class<?> clazz, final String method, final Class<?>... params) {
        try {
            clazz.getDeclaredMethod(method, params);
            return true;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasMethod(final Class<?> clazz, final Class<?> returnType, final String method, final Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(method, params).getReturnType().equals(returnType);
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }
}
