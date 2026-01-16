/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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

import com.viaversion.viaversion.UserConnectionViaAPI;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.logging.Logger;

public final class TestPlatform implements ViaPlatform<UserConnection> {

    private static final Logger log = Logger.getGlobal();
    private final AbstractViaConfig testConfig = new AbstractViaConfig(null, log) {
        @Override
        public void reload() {
        }
    };
    private final UserConnectionViaAPI testAPI = new UserConnectionViaAPI();

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
    public ViaAPI<UserConnection> getApi() {
        return testAPI;
    }

    @Override
    public AbstractViaConfig getConf() {
        return testConfig;
    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public boolean hasPlugin(final String name) {
        return false;
    }

    @Override
    public boolean couldBeReloading() {
        return false;
    }
}
