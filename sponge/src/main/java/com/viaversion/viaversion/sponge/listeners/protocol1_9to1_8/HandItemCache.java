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
package com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.sponge4.Sponge4ItemGrabber;
import com.viaversion.viaversion.sponge.listeners.protocol1_9to1_8.sponge5.Sponge5ItemGrabber;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandItemCache implements Runnable {
    public static boolean CACHE = false;
    private static Map<UUID, Item> handCache = new ConcurrentHashMap<>();
    private static Field GET_DAMAGE;
    private static Method GET_ID;
    private static ItemGrabber grabber;

    static {
        try {
            Class.forName("org.spongepowered.api.event.entity.DisplaceEntityEvent");
            grabber = new Sponge4ItemGrabber();
        } catch (ClassNotFoundException e) {
            grabber = new Sponge5ItemGrabber();
        }
    }

    public static Item getHandItem(UUID player) {
        return handCache.get(player);
    }

    @Override
    public void run() {
        List<UUID> players = new ArrayList<>(handCache.keySet());

        for (Player p : Sponge.server().onlinePlayers()) {
            handCache.put(p.uniqueId(), convert(grabber.getItem(p)));
            players.remove(p.uniqueId());
        }
        // Remove offline players
        for (UUID uuid : players) {
            handCache.remove(uuid);
        }
    }

    public static Item convert(ItemStack itemInHand) {
        if (itemInHand == null) return new DataItem(0, (byte) 0, (short) 0, null);
        if (GET_DAMAGE == null) {
            try {
                GET_DAMAGE = itemInHand.getClass().getDeclaredField("field_77991_e");
                GET_DAMAGE.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (GET_ID == null) {
            try {
                GET_ID = Class.forName("net.minecraft.item.Item").getDeclaredMethod("func_150891_b", Class.forName("net.minecraft.item.Item"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        int id = 0;
        if (GET_ID != null) {
            try {
                id = (int) GET_ID.invoke(null, itemInHand.getItem());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        int damage = 0;
        if (GET_DAMAGE != null) {
            try {
                damage = (int) GET_DAMAGE.get(itemInHand);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new DataItem(id, (byte) itemInHand.quantity(), (short) damage, null);
    }
}

