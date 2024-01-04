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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bungee.storage.BungeeStorage;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import java.lang.reflect.Method;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeEntityIdProvider extends EntityIdProvider {
    private static final Method GET_CLIENT_ENTITY_ID;

    static {
        try {
            GET_CLIENT_ENTITY_ID = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("getClientEntityId");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getEntityId(UserConnection user) throws Exception {
        BungeeStorage storage = user.get(BungeeStorage.class);
        ProxiedPlayer player = storage.getPlayer();

        return (int) GET_CLIENT_ENTITY_ID.invoke(player);
    }
}
