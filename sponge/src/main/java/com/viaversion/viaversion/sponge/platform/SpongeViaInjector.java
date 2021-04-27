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
package com.viaversion.viaversion.sponge.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.sponge.handlers.SpongeChannelInitializer;
import com.viaversion.viaversion.util.ListWrapper;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.ReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//TODO screams
public class SpongeViaInjector implements ViaInjector {
    private List<ChannelFuture> injectedFutures = new ArrayList<>();
    private List<Pair<Field, Object>> injectedLists = new ArrayList<>();

    @Override
    public void inject() throws Exception {
        try {
            Object connection = getServerConnection();
            if (connection == null) {
                throw new Exception("We failed to find the core component 'ServerConnection', please file an issue on our GitHub.");
            }
            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object value = field.get(connection);
                if (value instanceof List) {
                    // Inject the list
                    List wrapper = new ListWrapper((List) value) {
                        @Override
                        public void handleAdd(Object o) {
                            if (o instanceof ChannelFuture) {
                                try {
                                    injectChannelFuture((ChannelFuture) o);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    injectedLists.add(new Pair<>(field, connection));
                    field.set(connection, wrapper);
                    // Iterate through current list
                    synchronized (wrapper) {
                        for (Object o : (List) value) {
                            if (o instanceof ChannelFuture) {
                                injectChannelFuture((ChannelFuture) o);
                            } else {
                                break; // not the right list.
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    private void injectChannelFuture(ChannelFuture future) throws Exception {
        try {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;
            // Pick best
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                try {
                    ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                    bootstrapAcceptor = handler;
                } catch (Exception e) {
                    // Not this one
                }
            }
            // Default to first (Also allows blame to work)
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }
            try {
                ChannelInitializer<Channel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                ChannelInitializer newInit = new SpongeChannelInitializer(oldInit);

                ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
                injectedFutures.add(future);
            } catch (NoSuchFieldException e) {
                throw new Exception("Unable to find core component 'childHandler', please check your plugins. issue: " + bootstrapAcceptor.getClass().getName());

            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("We failed to inject ViaVersion, have you got late-bind enabled with something else?");
            throw e;
        }
    }

    @Override
    public boolean lateProtocolVersionSetting() {
        return true;
    }

    @Override
    public void uninject() {
        // TODO: Uninject from players currently online
        for (ChannelFuture future : injectedFutures) {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;
            // Pick best
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                try {
                    ChannelInitializer<Channel> oldInit = ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                    if (oldInit instanceof SpongeChannelInitializer) {
                        bootstrapAcceptor = handler;
                    }
                } catch (Exception e) {
                    // Not this one
                }
            }
            // Default to first
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }

            try {
                ChannelInitializer<Channel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                if (oldInit instanceof SpongeChannelInitializer) {
                    ReflectionUtil.set(bootstrapAcceptor, "childHandler", ((SpongeChannelInitializer) oldInit).getOriginal());
                }
            } catch (Exception e) {
                Via.getPlatform().getLogger().severe("Failed to remove injection handler, reload won't work with connections, please reboot!");
            }
        }
        injectedFutures.clear();

        for (Pair<Field, Object> pair : injectedLists) {
            try {
                Object o = pair.getKey().get(pair.getValue());
                if (o instanceof ListWrapper) {
                    pair.getKey().set(pair.getValue(), ((ListWrapper) o).getOriginalList());
                }
            } catch (IllegalAccessException e) {
                Via.getPlatform().getLogger().severe("Failed to remove injection, reload won't work with connections, please reboot!");
            }
        }

        injectedLists.clear();
    }

    public static Object getServer() throws Exception {
        return Sponge.getServer();
    }

    @Override
    public int getServerProtocolVersion() throws Exception {
        MinecraftVersion mcv = Sponge.getPlatform().getMinecraftVersion();
        try {
            return (int) mcv.getClass().getDeclaredMethod("getProtocol").invoke(mcv);
        } catch (Exception e) {
            throw new Exception("Failed to get server protocol", e);
        }
    }

    @Override
    public String getEncoderName() {
        return "encoder";
    }

    @Override
    public String getDecoderName() {
        return "decoder";
    }

    public static Object getServerConnection() throws Exception {
        Class<?> serverClazz = Class.forName("net.minecraft.server.MinecraftServer");
        Object server = getServer();
        Object connection = null;
        for (Method m : serverClazz.getDeclaredMethods()) {
            if (m.getReturnType() != null) {
                if (m.getReturnType().getSimpleName().equals("NetworkSystem")) {
                    if (m.getParameterTypes().length == 0) {
                        connection = m.invoke(server);
                    }
                }
            }
        }
        return connection;
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();

        // Generate information about current injections
        JsonArray injectedChannelInitializers = new JsonArray();
        for (ChannelFuture cf : injectedFutures) {
            JsonObject info = new JsonObject();
            info.addProperty("futureClass", cf.getClass().getName());
            info.addProperty("channelClass", cf.channel().getClass().getName());

            // Get information about the pipes for this channel future
            JsonArray pipeline = new JsonArray();
            for (String pipeName : cf.channel().pipeline().names()) {
                JsonObject pipe = new JsonObject();
                pipe.addProperty("name", pipeName);
                if (cf.channel().pipeline().get(pipeName) != null) {
                    pipe.addProperty("class", cf.channel().pipeline().get(pipeName).getClass().getName());
                    try {
                        Object child = ReflectionUtil.get(cf.channel().pipeline().get(pipeName), "childHandler", ChannelInitializer.class);
                        pipe.addProperty("childClass", child.getClass().getName());
                        if (child instanceof SpongeChannelInitializer) {
                            pipe.addProperty("oldInit", ((SpongeChannelInitializer) child).getOriginal().getClass().getName());
                        }
                    } catch (Exception e) {
                        // Don't display
                    }
                }
                // Add to the pipeline array
                pipeline.add(pipe);
            }
            info.add("pipeline", pipeline);

            // Add to the list
            injectedChannelInitializers.add(info);
        }
        data.add("injectedChannelInitializers", injectedChannelInitializers);

        // Generate information about lists we've injected into
        JsonObject wrappedLists = new JsonObject();
        JsonObject currentLists = new JsonObject();
        try {
            for (Pair<Field, Object> pair : injectedLists) {
                Object list = pair.getKey().get(pair.getValue());
                // Note down the current value (could be overridden by another plugin)
                currentLists.addProperty(pair.getKey().getName(), list.getClass().getName());
                // Also if it's not overridden we can display what's inside our list (possibly another plugin)
                if (list instanceof ListWrapper) {
                    wrappedLists.addProperty(pair.getKey().getName(), ((ListWrapper) list).getOriginalList().getClass().getName());
                }
            }
            data.add("wrappedLists", wrappedLists);
            data.add("currentLists", currentLists);
        } catch (Exception e) {
            // Ignored, fields won't be present
        }

        return data;
    }
}
