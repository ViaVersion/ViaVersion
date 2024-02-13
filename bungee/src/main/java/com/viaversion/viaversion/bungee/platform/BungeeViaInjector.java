/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.bungee.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bungee.handlers.BungeeChannelInitializer;
import com.viaversion.viaversion.util.ReflectionUtil;
import com.viaversion.viaversion.util.SetWrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import net.md_5.bungee.api.ProxyServer;

public class BungeeViaInjector implements ViaInjector {

    private static final Field LISTENERS_FIELD;
    private final List<Channel> injectedChannels = new ArrayList<>();

    static {
        try {
            LISTENERS_FIELD = ProxyServer.getInstance().getClass().getDeclaredField("listeners");
            LISTENERS_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to access listeners field.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void inject() throws ReflectiveOperationException {
        Set<Channel> listeners = (Set<Channel>) LISTENERS_FIELD.get(ProxyServer.getInstance());

        // Inject the list
        Set<Channel> wrapper = new SetWrapper<>(listeners, channel -> {
            try {
                injectChannel(channel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        LISTENERS_FIELD.set(ProxyServer.getInstance(), wrapper);

        // Iterate through current list
        for (Channel channel : listeners) {
            injectChannel(channel);
        }
    }

    @Override
    public void uninject() {
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Bungee without a reboot!");
    }

    private void injectChannel(Channel channel) throws ReflectiveOperationException {
        List<String> names = channel.pipeline().names();
        ChannelHandler bootstrapAcceptor = null;

        for (String name : names) {
            ChannelHandler handler = channel.pipeline().get(name);
            try {
                ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                bootstrapAcceptor = handler;
            } catch (Exception e) {
                // Not this one
            }
        }

        // Default to first
        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = channel.pipeline().first();
        }

        if (bootstrapAcceptor.getClass().getName().equals("net.md_5.bungee.query.QueryHandler")) {
            return;
        }

        try {
            ChannelInitializer<Channel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ChannelInitializer<Channel> newInit = new BungeeChannelInitializer(oldInit);

            ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
            this.injectedChannels.add(channel);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to find core component 'childHandler', please check your plugins. issue: " + bootstrapAcceptor.getClass().getName());
        }
    }

    @Override
    public ProtocolVersion getServerProtocolVersion() throws Exception {
        return ProtocolVersion.getProtocol(getBungeeSupportedVersions().get(0));
    }

    @Override
    public SortedSet<ProtocolVersion> getServerProtocolVersions() throws Exception {
        final SortedSet<ProtocolVersion> versions = new ObjectLinkedOpenHashSet<>();
        for (final Integer version : getBungeeSupportedVersions()) {
            versions.add(ProtocolVersion.getProtocol(version));
        }
        return versions;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getBungeeSupportedVersions() throws Exception {
        return ReflectionUtil.getStatic(Class.forName("net.md_5.bungee.protocol.ProtocolConstants"), "SUPPORTED_VERSION_IDS", List.class);
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();

        // Generate information about current injections
        JsonArray injectedChannelInitializers = new JsonArray();
        for (Channel channel : this.injectedChannels) {
            JsonObject channelInfo = new JsonObject();
            channelInfo.addProperty("channelClass", channel.getClass().getName());

            // Get information about the pipes for this channel
            JsonArray pipeline = new JsonArray();
            for (String pipeName : channel.pipeline().names()) {
                JsonObject handlerInfo = new JsonObject();
                handlerInfo.addProperty("name", pipeName);

                ChannelHandler channelHandler = channel.pipeline().get(pipeName);
                if (channelHandler == null) {
                    handlerInfo.addProperty("status", "INVALID");
                    continue;
                }

                handlerInfo.addProperty("class", channelHandler.getClass().getName());

                try {
                    Object child = ReflectionUtil.get(channelHandler, "childHandler", ChannelInitializer.class);
                    handlerInfo.addProperty("childClass", child.getClass().getName());
                    if (child instanceof BungeeChannelInitializer) {
                        handlerInfo.addProperty("oldInit", ((BungeeChannelInitializer) child).getOriginal().getClass().getName());
                    }
                } catch (ReflectiveOperationException e) {
                    // Don't display
                }

                pipeline.add(handlerInfo);
            }
            channelInfo.add("pipeline", pipeline);

            injectedChannelInitializers.add(channelInfo);
        }

        data.add("injectedChannelInitializers", injectedChannelInitializers);

        try {
            Object list = LISTENERS_FIELD.get(ProxyServer.getInstance());
            data.addProperty("currentList", list.getClass().getName());
            if (list instanceof SetWrapper) {
                data.addProperty("wrappedList", ((SetWrapper<?>) list).originalSet().getClass().getName());
            }
        } catch (ReflectiveOperationException ignored) {
            // Ignored
        }

        return data;
    }
}
