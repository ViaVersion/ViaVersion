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
package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.UserConnectionViaAPI;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.logging.Logger;

/**
 * Base implementation of a {@link ViaPlatform} for platforms using {@link UserConnection}.
 */
public abstract class UserConnectionViaVersionPlatform implements ViaPlatform<UserConnection> {

    private final UserConnectionViaAPI api = new UserConnectionViaAPI();
    private final AbstractViaConfig config;
    private final File dataFolder;
    private final Logger logger;

    protected UserConnectionViaVersionPlatform(final File dataFolder) {
        this.dataFolder = dataFolder;
        this.logger = createLogger("ViaVersion");
        this.config = createConfig();
    }

    protected AbstractViaConfig createConfig() {
        return new AbstractViaConfig(new File(this.getDataFolder(), "viaversion.yml"), this.getLogger());
    }

    @Override
    public abstract Logger createLogger(String name);

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ViaAPI<UserConnection> getApi() {
        return api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}
