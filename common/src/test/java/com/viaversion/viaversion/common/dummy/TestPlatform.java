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
package com.viaversion.viaversion.common.dummy;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

public final class TestPlatform implements ViaPlatform {

    private static final Logger log = Logger.getGlobal();
    private final TestConfig testConfig = new TestConfig(null);

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public String getPlatformName() {
        return "Test";
    }

    @Override
    public String getPlatformVersion() {
        return "test";
    }

    @Override
    public String getPluginVersion() {
        return "test";
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        return null;
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        return null;
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long ticks) {
        return null;
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long ticks) {
        return null;
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return new ViaCommandSender[0];
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return false;
    }

    @Override
    public ViaAPI getApi() {
        return new ViaAPIBase() {
            @Override
            public int getPlayerVersion(Object player) {
                return 0;
            }

            @Override
            public void sendRawPacket(Object player, ByteBuf packet) {
            }
        };
    }

    @Override
    public ViaVersionConfig getConf() {
        return testConfig;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return null;
    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public void onReload() {
    }

    @Override
    public JsonObject getDump() {
        return null;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return false;
    }
}
