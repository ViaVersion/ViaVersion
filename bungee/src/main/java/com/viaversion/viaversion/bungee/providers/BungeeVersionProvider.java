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
package com.viaversion.viaversion.bungee.providers;

import com.google.common.collect.Lists;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import com.viaversion.viaversion.util.ReflectionUtil;
import net.md_5.bungee.api.ProxyServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BungeeVersionProvider extends BaseVersionProvider {
    private static Class<?> ref;

    static {
        try {
            ref = Class.forName("net.md_5.bungee.protocol.ProtocolConstants");
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Could not detect the ProtocolConstants class");
            e.printStackTrace();
        }
    }

    @Override
    public int getClosestServerProtocol(UserConnection user) throws Exception {
        if (ref == null)
            return super.getClosestServerProtocol(user);
        // TODO Have one constant list forever until restart? (Might limit plugins if they change this)
        List<Integer> list = ReflectionUtil.getStatic(ref, "SUPPORTED_VERSION_IDS", List.class);
        List<Integer> sorted = new ArrayList<>(list);
        Collections.sort(sorted);

        ProtocolInfo info = user.getProtocolInfo();

        // Bungee supports it
        if (sorted.contains(info.getProtocolVersion()))
            return info.getProtocolVersion();

        // Older than bungee supports, get the lowest version
        if (info.getProtocolVersion() < sorted.get(0)) {
            return getLowestSupportedVersion();
        }

        // Loop through all protocols to get the closest protocol id that bungee supports (and that viaversion does too)

        // TODO: This needs a better fix, i.e checking ProtocolRegistry to see if it would work.
        // This is more of a workaround for snapshot support by bungee.
        for (Integer protocol : Lists.reverse(sorted)) {
            if (info.getProtocolVersion() > protocol && ProtocolVersion.isRegistered(protocol))
                return protocol;
        }

        Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + info.getProtocolVersion());
        return info.getProtocolVersion();
    }

    public static int getLowestSupportedVersion() {
        List<Integer> list;
        try {
            list = ReflectionUtil.getStatic(ref, "SUPPORTED_VERSION_IDS", List.class);
            return list.get(0);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // Fallback
        return ProxyServer.getInstance().getProtocolVersion();
    }
}
