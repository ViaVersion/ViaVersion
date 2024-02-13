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
package com.viaversion.viaversion.sponge.platform;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.platform.LegacyViaInjector;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.sponge.handlers.SpongeChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;

public class SpongeViaInjector extends LegacyViaInjector {

    @Override
    public ProtocolVersion getServerProtocolVersion() throws ReflectiveOperationException {
        MinecraftVersion version = Sponge.platform().minecraftVersion();

        // 'protocolVersion' method was exposed to the API in a 1.19.4 build and 'getProtocol' no longer exists in the impl.
        try {
            return ProtocolVersion.getProtocol((int) version.getClass().getDeclaredMethod("getProtocol").invoke(version));
        } catch (NoSuchMethodException e) {
            return ProtocolVersion.getProtocol((int) version.getClass().getDeclaredMethod("protocolVersion").invoke(version));
        }
    }

    @Override
    protected @Nullable Object getServerConnection() throws ReflectiveOperationException {
        Class<?> serverClazz = Class.forName("net.minecraft.server.MinecraftServer");
        return serverClazz.getDeclaredMethod("getConnection").invoke(Sponge.server());
    }

    @Override
    protected WrappedChannelInitializer createChannelInitializer(ChannelInitializer<Channel> oldInitializer) {
        return new SpongeChannelInitializer(oldInitializer);
    }

    @Override
    protected void blame(ChannelHandler bootstrapAcceptor) {
        throw new RuntimeException("Unable to find core component 'childHandler', please check your plugins. Issue: " + bootstrapAcceptor.getClass().getName());
    }

    @Override
    public String getEncoderName() {
        return "encoder";
    }

    @Override
    public String getDecoderName() {
        return "decoder";
    }
}