/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;

public final class ProtocolSupportUtil {
    private static final Method PROTOCOL_VERSION_METHOD;
    private static final Method GET_ID_METHOD;

    static {
        Method protocolVersionMethod = null;
        Method getIdMethod = null;
        try {
            protocolVersionMethod = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class);
            getIdMethod = Class.forName("protocolsupport.api.ProtocolVersion").getMethod("getId");
        } catch (ReflectiveOperationException e) {
            // ProtocolSupport not installed.
        }
        PROTOCOL_VERSION_METHOD = protocolVersionMethod;
        GET_ID_METHOD = getIdMethod;
    }

    public static int getProtocolVersion(Player player) {
        if (PROTOCOL_VERSION_METHOD == null) {
            return -1;
        }
        try {
            Object version = PROTOCOL_VERSION_METHOD.invoke(null, player);
            return (int) GET_ID_METHOD.invoke(version);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
