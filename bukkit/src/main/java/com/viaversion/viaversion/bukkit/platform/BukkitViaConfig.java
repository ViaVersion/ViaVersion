/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BukkitViaConfig extends AbstractViaConfig {
    private boolean quickMoveActionFix;
    private boolean hitboxFix1_9;
    private boolean hitboxFix1_14;
    private String blockConnectionMethod;
    private boolean armorToggleFix;
    private boolean registerUserConnectionOnJoin;
    private boolean useNewDeathMessages;
    private boolean itemCache;
    private boolean nmsPlayerTicking;

    public BukkitViaConfig(final File folder, final Logger logger) {
        super(new File(folder, "config.yml"), logger);
    }

    @Override
    protected void loadFields() {
        super.loadFields();
        registerUserConnectionOnJoin = getBoolean("register-userconnections-on-join", true);
        quickMoveActionFix = getBoolean("quick-move-action-fix", false);
        hitboxFix1_9 = getBoolean("change-1_9-hitbox", false);
        hitboxFix1_14 = getBoolean("change-1_14-hitbox", false);
        blockConnectionMethod = getString("blockconnection-method", "packet");
        armorToggleFix = getBoolean("armor-toggle-fix", true);
        useNewDeathMessages = getBoolean("use-new-deathmessages", true);
        itemCache = getBoolean("item-cache", true);
        nmsPlayerTicking = getBoolean("nms-player-ticking", true);
    }

    @Override
    public boolean shouldRegisterUserConnectionOnJoin() {
        return registerUserConnectionOnJoin;
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
    public boolean isArmorToggleFix() {
        return armorToggleFix;
    }

    @Override
    public boolean isShowNewDeathMessages() {
        return useNewDeathMessages;
    }

    @Override
    public boolean isItemCache() {
        return itemCache;
    }

    @Override
    public boolean isNMSPlayerTicking() {
        return nmsPlayerTicking;
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return VELOCITY_ONLY_OPTIONS;
    }
}
