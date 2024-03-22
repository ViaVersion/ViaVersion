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
package com.viaversion.viaversion.bungee.providers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/*
    This solves the wrong main hand issue when you join with BungeeCord on a 1.8 server, and switch to a 1.9 or higher.
 */
public class BungeeMainHandProvider extends MainHandProvider {
    private static Method getSettings;
    private static Method setMainHand;

    static {
        try {
            getSettings = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("getSettings");
            setMainHand = Class.forName("net.md_5.bungee.protocol.packet.ClientSettings").getDeclaredMethod("setMainHand", int.class);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setMainHand(UserConnection user, int hand) {
        ProtocolInfo info = user.getProtocolInfo();
        if (info == null || info.getUuid() == null) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(info.getUuid());
        if (player == null) return;
        try {
            Object settings = getSettings.invoke(player);
            if (settings != null) {
                setMainHand.invoke(settings, hand);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to set main hand for " + player.getName(), e);
        }
    }
}
