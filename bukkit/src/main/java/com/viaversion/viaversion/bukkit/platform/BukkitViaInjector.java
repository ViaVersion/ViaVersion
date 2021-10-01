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
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.bukkit.handlers.BukkitChannelInitializer;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.platform.LegacyViaInjector;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.util.ReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BukkitViaInjector extends LegacyViaInjector {
    private boolean protocolLib;

    @Override
    public void inject() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            PaperViaInjector.setPaperChannelInitializeListener();
            return;
        }

        super.inject();
    }

    @Override
    public void uninject() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            PaperViaInjector.removePaperChannelInitializeListener();
            return;
        }

        super.uninject();
    }

    @Override
    public int getServerProtocolVersion() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_PROTOCOL_METHOD) {
            //noinspection deprecation
            return Bukkit.getUnsafe().getProtocolVersion();
        }

        // Time to go on a journey! The protocol version is hidden inside an int in ServerPing.ServerData
        // Grab a static instance of the server
        Class<?> serverClazz = NMSUtil.nms("MinecraftServer", "net.minecraft.server.MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");

        // Grab the ping class and find the field to access it
        Class<?> pingClazz = NMSUtil.nms(
                "ServerPing",
                "net.minecraft.network.protocol.status.ServerPing"
        );
        Object ping = null;
        for (Field field : serverClazz.getDeclaredFields()) {
            if (field.getType() == pingClazz) {
                field.setAccessible(true);
                ping = field.get(server);
                break;
            }
        }

        // Now get the ServerData inside ServerPing
        Class<?> serverDataClass = NMSUtil.nms(
                "ServerPing$ServerData",
                "net.minecraft.network.protocol.status.ServerPing$ServerData"
        );
        Object serverData = null;
        for (Field field : pingClazz.getDeclaredFields()) {
            if (field.getType() == serverDataClass) {
                field.setAccessible(true);
                serverData = field.get(ping);
                break;
            }
        }

        // Get protocol version field
        for (Field field : serverDataClass.getDeclaredFields()) {
            if (field.getType() != int.class) {
                continue;
            }

            field.setAccessible(true);
            int protocolVersion = (int) field.get(serverData);
            if (protocolVersion != -1) {
                return protocolVersion;
            }
        }
        throw new RuntimeException("Failed to get server");
    }

    @Override
    public String getDecoderName() {
        return protocolLib ? "protocol_lib_decoder" : "decoder";
    }

    @Override
    protected @Nullable Object getServerConnection() throws ReflectiveOperationException {
        Class<?> serverClass = NMSUtil.nms(
                "MinecraftServer",
                "net.minecraft.server.MinecraftServer"
        );
        Class<?> connectionClass = NMSUtil.nms(
                "ServerConnection",
                "net.minecraft.server.network.ServerConnection"
        );

        Object server = ReflectionUtil.invokeStatic(serverClass, "getServer");
        for (Method method : serverClass.getDeclaredMethods()) {
            if (method.getReturnType() != connectionClass || method.getParameterTypes().length != 0) {
                continue;
            }

            // We need the method that initiates the connection if not yet set
            Object connection = method.invoke(server);
            if (connection != null) {
                return connection;
            }
        }
        return null;
    }

    @Override
    protected WrappedChannelInitializer createChannelInitializer(ChannelInitializer<Channel> oldInitializer) {
        return new BukkitChannelInitializer(oldInitializer);
    }

    @Override
    protected void blame(ChannelHandler bootstrapAcceptor) throws ReflectiveOperationException {
        // Let's find who to blame!
        ClassLoader classLoader = bootstrapAcceptor.getClass().getClassLoader();
        if (classLoader.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
            PluginDescriptionFile description = ReflectionUtil.get(classLoader, "description", PluginDescriptionFile.class);
            throw new RuntimeException("Unable to inject, due to " + bootstrapAcceptor.getClass().getName() + ", try without the plugin " + description.getName() + "?");
        } else {
            throw new RuntimeException("Unable to find core component 'childHandler', please check your plugins. issue: " + bootstrapAcceptor.getClass().getName());
        }
    }

    public boolean isBinded() {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            return true;
        }
        try {
            Object connection = getServerConnection();
            if (connection == null) {
                return false;
            }

            for (Field field : connection.getClass().getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                field.setAccessible(true);
                List<?> value = (List<?>) field.get(connection);
                // Check if the list has at least one element
                synchronized (value) {
                    if (!value.isEmpty() && value.get(0) instanceof ChannelFuture) {
                        return true;
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setProtocolLib(boolean protocolLib) {
        this.protocolLib = protocolLib;
    }
}