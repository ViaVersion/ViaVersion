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
package com.viaversion.viaversion.sponge.listeners;

import com.viaversion.viaversion.SpongePlugin;
import com.viaversion.viaversion.ViaListener;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.Protocol;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.lang.reflect.Field;

public class ViaSpongeListener extends ViaListener {
    private static Field entityIdField;

    private final SpongePlugin plugin;

    public ViaSpongeListener(SpongePlugin plugin, Class<? extends Protocol> requiredPipeline) {
        super(requiredPipeline);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        if (isRegistered()) return;

        Sponge.eventManager().registerListeners(plugin.getPluginContainer(), this);
        setRegistered(true);
    }

    // Hey sponge, please create a getEntityId method :'(
    protected int getEntityId(Player p) {
        try {
            if (entityIdField == null) {
                entityIdField = p.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("field_145783_c");
                entityIdField.setAccessible(true);
            }

            return entityIdField.getInt(p);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Could not get the entity id, please report this on our Github");
            e.printStackTrace();
        }

        Via.getPlatform().getLogger().severe("Could not get the entity id, please report this on our Github");
        return -1;
    }
}
