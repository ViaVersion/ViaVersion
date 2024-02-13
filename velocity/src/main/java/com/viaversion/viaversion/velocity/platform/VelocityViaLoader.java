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
package com.viaversion.viaversion.velocity.platform;

import com.velocitypowered.api.plugin.PluginContainer;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import com.viaversion.viaversion.velocity.listeners.UpdateListener;
import com.viaversion.viaversion.velocity.providers.VelocityBossBarProvider;
import com.viaversion.viaversion.velocity.providers.VelocityVersionProvider;

public class VelocityViaLoader implements ViaPlatformLoader {

    @Override
    public void load() {
        Object plugin = VelocityPlugin.PROXY.getPluginManager()
                .getPlugin("viaversion").flatMap(PluginContainer::getInstance).get();

        final ProtocolVersion protocolVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
        if (protocolVersion.olderThan(ProtocolVersion.v1_9)) {
            Via.getManager().getProviders().use(BossBarProvider.class, new VelocityBossBarProvider());
        }

        Via.getManager().getProviders().use(VersionProvider.class, new VelocityVersionProvider());
        // We probably don't need a EntityIdProvider because velocity sends a Join packet on server change
        // We don't need main hand patch because Join Game packet makes client send hand data again

        VelocityPlugin.PROXY.getEventManager().register(plugin, new UpdateListener());

        int pingInterval = ((VelocityViaConfig) Via.getPlatform().getConf()).getVelocityPingInterval();
        if (pingInterval > 0) {
            Via.getPlatform().runRepeatingAsync(
                    () -> Via.proxyPlatform().protocolDetectorService().probeAllServers(),
                    pingInterval * 20L);
        }
    }

    @Override
    public void unload() {
        // Probably not useful, there's no ProxyReloadEvent
    }
}
