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
package com.viaversion.viaversion.bukkit.providers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.tasks.v1_11_1to1_12.BukkitInventoryUpdateTask;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.provider.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.storage.ItemTransactionStorage;
import com.viaversion.viaversion.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class BukkitInventoryQuickMoveProvider extends InventoryQuickMoveProvider {

    private final Map<UUID, BukkitInventoryUpdateTask> updateTasks = new ConcurrentHashMap<>();
    private final boolean supported;
    // packet class
    private Class<?> windowClickPacketClass;
    private Object clickTypeEnum;
    // Use for nms
    private Method nmsItemMethod;
    private Method craftPlayerHandle;
    private Field connection;
    private Method packetMethod;

    public BukkitInventoryQuickMoveProvider() {
        this.supported = isSupported();
        setupReflection();
    }

    @Override
    public boolean registerQuickMoveAction(short windowId, short slotId, short actionId, UserConnection userConnection) {
        if (!supported) {
            return false;
        }
        if (slotId < 0) { // clicked out of inv slot
            return false;
        }
        if (windowId == 0) {
            // windowId is always 0 for player inventory.
            // This has almost definitely something to do with the offhand slot.
            if (slotId >= 36 && slotId <= 45) {
                final ProtocolVersion protocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
                // this seems to be working just fine.
                if (protocol.equalTo(ProtocolVersion.v1_8)) {
                    return false;
                }
            }
        }
        ProtocolInfo info = userConnection.getProtocolInfo();
        UUID uuid = info.getUuid();
        BukkitInventoryUpdateTask updateTask = updateTasks.get(uuid);
        final boolean registered = updateTask != null;
        if (!registered) {
            updateTask = new BukkitInventoryUpdateTask(this, uuid);
            updateTasks.put(uuid, updateTask);
        }
        updateTask.addItem(windowId, slotId, actionId);
        if (!registered) {
            Via.getPlatform().runSync(updateTask);
        }
        return true;
    }

    public Object buildWindowClickPacket(Player p, ItemTransactionStorage storage) {
        if (!supported) {
            return null;
        }
        InventoryView inv = p.getOpenInventory();
        short slotId = storage.slotId();
        Inventory tinv = inv.getTopInventory();
        InventoryType tinvtype = tinv == null ? null : tinv.getType(); // can this even be null?
        if (tinvtype != null) {
            final ProtocolVersion protocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
            if (protocol.equalTo(ProtocolVersion.v1_8)) {
                if (tinvtype == InventoryType.BREWING) {
                    // 1.9 added the blaze powder slot to brewing stand fix for 1.8 servers
                    if (slotId >= 5 && slotId <= 40) {
                        slotId -= 1;
                    }
                }
            }
        }
        ItemStack itemstack = null;
        // must be after top inventory slot check
        if (slotId <= inv.countSlots()) {
            itemstack = inv.getItem(slotId);
        } else {
            // if not true we got too many slots (version inventory slot changes)?
            String cause = "Too many inventory slots: slotId: " + slotId + " invSlotCount: " + inv.countSlots()
                + " invType: " + inv.getType() + " topInvType: " + tinvtype;
            Via.getPlatform().getLogger().severe("Failed to get an item to create a window click packet. Please report this issue to the ViaVersion Github: " + cause);
        }
        Object packet = null;
        try {
            packet = windowClickPacketClass.getDeclaredConstructor().newInstance();
            Object nmsItem = itemstack == null ? null : nmsItemMethod.invoke(null, itemstack);
            ReflectionUtil.set(packet, "a", (int) storage.windowId());
            ReflectionUtil.set(packet, "slot", (int) slotId);
            ReflectionUtil.set(packet, "button", 0); // shift + left mouse click
            ReflectionUtil.set(packet, "d", storage.actionId());
            ReflectionUtil.set(packet, "item", nmsItem);
            final ProtocolVersion protocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
            if (protocol.equalTo(ProtocolVersion.v1_8)) {
                ReflectionUtil.set(packet, "shift", 1);
            } else if (protocol.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
                ReflectionUtil.set(packet, "shift", clickTypeEnum);
            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to create a window click packet. Please report this issue to the ViaVersion Github: " + e.getMessage(), e);
        }
        return packet;
    }

    public boolean sendPacketToServer(Player p, Object packet) {
        if (packet == null) {
            // let the other packets pass through
            return true;
        }
        try {
            Object entityPlayer = craftPlayerHandle.invoke(p);
            Object playerConnection = connection.get(entityPlayer);
            // send
            packetMethod.invoke(playerConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to send packet to server", e);
            return false;
        }
        return true;
    }

    public void onTaskExecuted(UUID uuid) {
        updateTasks.remove(uuid);
    }

    private void setupReflection() {
        if (!supported) {
            return;
        }
        try {
            this.windowClickPacketClass = NMSUtil.nms("PacketPlayInWindowClick");
            final ProtocolVersion protocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
            if (protocol.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
                Class<?> eclassz = NMSUtil.nms("InventoryClickType");
                Object[] constants = eclassz.getEnumConstants();
                this.clickTypeEnum = constants[1]; // QUICK_MOVE
            }
            Class<?> craftItemStack = NMSUtil.obc("inventory.CraftItemStack");
            this.nmsItemMethod = craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't find required inventory classes", e);
        }
        try {
            this.craftPlayerHandle = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find CraftPlayer", e);
        }
        try {
            this.connection = NMSUtil.nms("EntityPlayer").getDeclaredField("playerConnection");
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find Player Connection", e);
        }
        try {
            this.packetMethod = NMSUtil.nms("PlayerConnection").getDeclaredMethod("a", windowClickPacketClass);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find CraftPlayer", e);
        }
    }

    private boolean isSupported() {
        final ProtocolVersion protocol = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
        return protocol.newerThanOrEqualTo(ProtocolVersion.v1_8) && protocol.olderThanOrEqualTo(ProtocolVersion.v1_11_1);
    }
}
