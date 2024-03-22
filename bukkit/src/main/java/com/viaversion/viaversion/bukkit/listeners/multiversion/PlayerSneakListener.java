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
package com.viaversion.viaversion.bukkit.listeners.multiversion;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSneakListener extends ViaBukkitListener {
    private static final float STANDING_HEIGHT = 1.8F;
    private static final float HEIGHT_1_14 = 1.5F;
    private static final float HEIGHT_1_9 = 1.6F;
    private static final float DEFAULT_WIDTH = 0.6F;

    private final boolean is1_9Fix;
    private final boolean is1_14Fix;
    private Map<Player, Boolean> sneaking; // true = 1.14+, else false
    private Set<UUID> sneakingUuids;
    private final Method getHandle;
    private Method setSize;

    private boolean useCache;

    public PlayerSneakListener(ViaVersionPlugin plugin, boolean is1_9Fix, boolean is1_14Fix) throws ReflectiveOperationException {
        super(plugin, null);
        this.is1_9Fix = is1_9Fix;
        this.is1_14Fix = is1_14Fix;

        final String packageName = plugin.getServer().getClass().getPackage().getName();
        getHandle = Class.forName(packageName + ".entity.CraftPlayer").getMethod("getHandle");

        final Class<?> entityPlayerClass = Class.forName(packageName
                .replace("org.bukkit.craftbukkit", "net.minecraft.server") + ".EntityPlayer");
        try {
            setSize = entityPlayerClass.getMethod("setSize", Float.TYPE, Float.TYPE);
        } catch (NoSuchMethodException e) {
            // Don't catch this one
            setSize = entityPlayerClass.getMethod("a", Float.TYPE, Float.TYPE);
        }


        // From 1.9 upwards the server hitbox is set in every entity tick, so we have to reset it everytime
        if (Via.getAPI().getServerVersion().lowestSupportedProtocolVersion().newerThan(ProtocolVersion.v1_8)) {
            sneaking = new WeakHashMap<>();
            useCache = true;
            plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                for (Map.Entry<Player, Boolean> entry : sneaking.entrySet()) {
                    setHeight(entry.getKey(), entry.getValue() ? HEIGHT_1_14 : HEIGHT_1_9);
                }
            }, 1, 1);
        }

        // Suffocation removal only required for 1.14+ clients.
        if (is1_14Fix) {
            sneakingUuids = new HashSet<>();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UserConnection userConnection = getUserConnection(player);
        if (userConnection == null) return;
        ProtocolInfo info = userConnection.getProtocolInfo();
        if (info == null) return;

        ProtocolVersion protocolVersion = info.protocolVersion();
        if (is1_14Fix && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_14)) {
            setHeight(player, event.isSneaking() ? HEIGHT_1_14 : STANDING_HEIGHT);
            if (event.isSneaking())
                sneakingUuids.add(player.getUniqueId());
            else
                sneakingUuids.remove(player.getUniqueId());

            if (!useCache) return;
            if (event.isSneaking())
                sneaking.put(player, true);
            else
                sneaking.remove(player);
        } else if (is1_9Fix && protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            setHeight(player, event.isSneaking() ? HEIGHT_1_9 : STANDING_HEIGHT);
            if (!useCache) return;
            if (event.isSneaking())
                sneaking.put(player, false);
            else
                sneaking.remove(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerDamage(EntityDamageEvent event) {
        if (!is1_14Fix) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) return;
        if (event.getEntityType() != EntityType.PLAYER) return;

        Player player = (Player) event.getEntity();
        if (!sneakingUuids.contains(player.getUniqueId())) return;

        // Don't cancel when they should actually be suffocating; Essentially cancel when the head is in the top block only ever so slightly
        // ~0.041 should suffice, but gotta stay be safe
        double y = player.getEyeLocation().getY() + 0.045;
        y -= (int) y;
        if (y < 0.09) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        if (sneaking != null)
            sneaking.remove(event.getPlayer());
        if (sneakingUuids != null)
            sneakingUuids.remove(event.getPlayer().getUniqueId());
    }

    private void setHeight(Player player, float height) {
        try {
            setSize.invoke(getHandle.invoke(player), DEFAULT_WIDTH, height);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to set player height", e);
        }
    }
}