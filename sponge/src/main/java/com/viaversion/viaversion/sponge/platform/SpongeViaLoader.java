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

import com.viaversion.viaversion.SpongePlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import com.viaversion.viaversion.sponge.listeners.UpdateListener;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.BlockListener;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.DeathListener;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.HandItemCache;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.sponge4.Sponge4ArmorListener;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.sponge5.Sponge5ArmorListener;
import com.viaversion.viaversion.sponge.providers.SpongeViaMovementTransmitter;
import org.spongepowered.api.Sponge;

import java.util.HashSet;
import java.util.Set;

public class SpongeViaLoader implements ViaPlatformLoader {

    private final SpongePlugin plugin;

    private final Set<Object> listeners = new HashSet<>();
    private final Set<PlatformTask> tasks = new HashSet<>();

    public SpongeViaLoader(SpongePlugin plugin) {
        this.plugin = plugin;
    }

    private void registerListener(Object listener) {
        Sponge.getEventManager().registerListeners(plugin, storeListener(listener));
    }

    private <T> T storeListener(T listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void load() {
        // Update Listener
        registerListener(new UpdateListener());

        /* 1.9 client to 1.8 server */
        if (Via.getAPI().getServerVersion().lowestSupportedVersion() < ProtocolVersion.v1_9.getVersion()) {
            try {
                Class.forName("org.spongepowered.api.event.entity.DisplaceEntityEvent");
                storeListener(new Sponge4ArmorListener()).register();
            } catch (ClassNotFoundException e) {
                storeListener(new Sponge5ArmorListener(plugin)).register();
            }
            storeListener(new DeathListener(plugin)).register();
            storeListener(new BlockListener(plugin)).register();

            if (plugin.getConf().isItemCache()) {
                tasks.add(Via.getPlatform().runRepeatingSync(new HandItemCache(), 2L)); // Updates players items :)
                HandItemCache.CACHE = true;
            }
        }

        /* Providers */
        if (Via.getAPI().getServerVersion().lowestSupportedVersion() < ProtocolVersion.v1_9.getVersion()) {
            Via.getManager().getProviders().use(MovementTransmitterProvider.class, new SpongeViaMovementTransmitter());

            Via.getManager().getProviders().use(HandItemProvider.class, new HandItemProvider() {
                @Override
                public Item getHandItem(final UserConnection info) {
                    if (HandItemCache.CACHE) {
                        return HandItemCache.getHandItem(info.getProtocolInfo().getUuid());
                    } else {
                        return super.getHandItem(info);
                    }
                }
            });
        }
    }

    public void unload() {
        listeners.forEach(Sponge.getEventManager()::unregisterListeners);
        listeners.clear();
        tasks.forEach(PlatformTask::cancel);
        tasks.clear();
    }
}