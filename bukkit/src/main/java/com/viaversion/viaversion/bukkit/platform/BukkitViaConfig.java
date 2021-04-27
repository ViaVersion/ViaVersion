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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BukkitViaConfig extends AbstractViaConfig {
    private static final List<String> UNSUPPORTED = Arrays.asList("bungee-ping-interval", "bungee-ping-save", "bungee-servers", "velocity-ping-interval", "velocity-ping-save", "velocity-servers");
    private boolean antiXRay;
    private boolean quickMoveActionFix;
    private boolean hitboxFix1_9;
    private boolean hitboxFix1_14;
    private String blockConnectionMethod;

    public BukkitViaConfig() {
        super(new File(((Plugin) Via.getPlatform()).getDataFolder(), "config.yml"));
        reloadConfig();
    }

    @Override
    protected void loadFields() {
        super.loadFields();
        antiXRay = getBoolean("anti-xray-patch", true);
        quickMoveActionFix = getBoolean("quick-move-action-fix", false);
        hitboxFix1_9 = getBoolean("change-1_9-hitbox", false);
        hitboxFix1_14 = getBoolean("change-1_14-hitbox", false);
        blockConnectionMethod = getString("blockconnection-method", "packet");
    }

    @Override
    public URL getDefaultConfigURL() {
        return BukkitViaConfig.class.getClassLoader().getResource("assets/viaversion/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public boolean isAntiXRay() {
        return antiXRay;
    }

    @Override
    public boolean is1_12QuickMoveActionFix() {
        return quickMoveActionFix;
    }

    @Override
    public boolean is1_9HitboxFix() {
        return hitboxFix1_9;
    }

    @Override
    public boolean is1_14HitboxFix() {
        return hitboxFix1_14;
    }

    @Override
    public String getBlockConnectionMethod() {
        return blockConnectionMethod;
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }
}
