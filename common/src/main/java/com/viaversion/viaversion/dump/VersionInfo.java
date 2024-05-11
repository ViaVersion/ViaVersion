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

public record VersionInfo(String javaVersion, String operatingSystem, VersionType versionType,
                          int serverProtocol, String serverVersion,
                          Set<String> enabledProtocols, String platformName,
                          String platformVersion, String pluginVersion,
                          String implementationVersion, Set<String> subPlatforms) {
}

