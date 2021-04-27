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
package com.viaversion.viaversion.bungee.platform;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.bungee.handlers.BungeeChannelInitializer;
import com.viaversion.viaversion.compatibility.ForcefulFieldModifier;
import com.viaversion.viaversion.compatibility.unsafe.UnsafeBackedForcefulFieldModifier;
import com.viaversion.viaversion.util.ReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.lang.reflect.Field;
import java.util.List;

public class BungeeViaInjector implements ViaInjector {

    private final ForcefulFieldModifier forcefulFieldModifier;

    public BungeeViaInjector() {
        try {
            this.forcefulFieldModifier = new UnsafeBackedForcefulFieldModifier();
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot create a modifier accessor", ex);
        }
    }

    @Override
    public void inject() throws Exception {
        try {
            Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
            Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
            field.setAccessible(true);

            BungeeChannelInitializer newInit = new BungeeChannelInitializer((ChannelInitializer<Channel>) field.get(null));

            this.forcefulFieldModifier.setField(field, null, newInit);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    @Override
    public void uninject() {
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Bungee without a reboot!");
    }


    @Override
    public int getServerProtocolVersion() throws Exception {
        return getBungeeSupportedVersions().get(0);
    }

    @Override
    public IntSortedSet getServerProtocolVersions() throws Exception {
        return new IntLinkedOpenHashSet(getBungeeSupportedVersions());
    }

    private List<Integer> getBungeeSupportedVersions() throws Exception {
        return ReflectionUtil.getStatic(Class.forName("net.md_5.bungee.protocol.ProtocolConstants"), "SUPPORTED_VERSION_IDS", List.class);
    }

    @Override
    public String getEncoderName() {
        return "via-encoder";
    }

    @Override
    public String getDecoderName() {
        return "via-decoder";
    }

    private ChannelInitializer<Channel> getChannelInitializer() throws Exception {
        Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
        Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
        field.setAccessible(true);
        return (ChannelInitializer<Channel>) field.get(null);
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();
        try {
            ChannelInitializer<Channel> initializer = getChannelInitializer();
            data.addProperty("currentInitializer", initializer.getClass().getName());
            if (initializer instanceof BungeeChannelInitializer) {
                data.addProperty("originalInitializer", ((BungeeChannelInitializer) initializer).getOriginal().getClass().getName());
            }
        } catch (Exception e) {
            // Ignored, not printed in the dump
        }
        return data;
    }
}
