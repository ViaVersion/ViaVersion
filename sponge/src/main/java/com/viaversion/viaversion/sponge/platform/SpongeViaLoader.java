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

import com.viaversion.viaversion.SpongePlugin;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.sponge.listeners.UpdateListener;
import java.util.HashSet;
import java.util.Set;
import org.spongepowered.api.Sponge;

public class SpongeViaLoader implements ViaPlatformLoader {

    private final SpongePlugin plugin;

    private final Set<Object> listeners = new HashSet<>();
    private final Set<PlatformTask> tasks = new HashSet<>();

    public SpongeViaLoader(SpongePlugin plugin) {
        this.plugin = plugin;
    }

    private void registerListener(Object listener) {
        Sponge.eventManager().registerListeners(plugin.container(), storeListener(listener));
    }

    private <T> T storeListener(T listener) {
        listeners.add(listener);
        return listener;
    }

    @Override
    public void load() {
        // Update Listener
        registerListener(new UpdateListener());
    }

    @Override
    public void unload() {
        listeners.forEach(Sponge.eventManager()::unregisterListeners);
        listeners.clear();
        tasks.forEach(PlatformTask::cancel);
        tasks.clear();
    }
}