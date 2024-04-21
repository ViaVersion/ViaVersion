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
package com.viaversion.viaversion.dump;

import com.viaversion.viaversion.api.protocol.version.VersionType;
import java.util.Set;

public class VersionInfo {
    private final String javaVersion;
    private final String operatingSystem;
    private final VersionType versionType;
    private final int serverProtocol;
    private final String serverVersion;
    private final Set<String> enabledVersions;
    private final String platformName;
    private final String platformVersion;
    private final String pluginVersion;
    private final String implementationVersion;
    private final Set<String> subPlatforms;

    public VersionInfo(String javaVersion, String operatingSystem, VersionType versionType, int serverProtocol, String serverVersion,
                       Set<String> enabledVersions, String platformName, String platformVersion, String pluginVersion, String implementationVersion,
                       Set<String> subPlatforms) {
        this.javaVersion = javaVersion;
        this.operatingSystem = operatingSystem;
        this.serverProtocol = serverProtocol;
        this.versionType = versionType;
        this.serverVersion = serverVersion;
        this.enabledVersions = enabledVersions;
        this.platformName = platformName;
        this.platformVersion = platformVersion;
        this.pluginVersion = pluginVersion;
        this.implementationVersion = implementationVersion;
        this.subPlatforms = subPlatforms;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public int getServerProtocol() {
        return serverProtocol;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public Set<String> getEnabledVersions() {
        return enabledVersions;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String getImplementationVersion() {
        return implementationVersion;
    }

    public Set<String> getSubPlatforms() {
        return subPlatforms;
    }
}

