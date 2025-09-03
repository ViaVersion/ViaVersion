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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitViaMovementTransmitter extends MovementTransmitterProvider {
    private static boolean USE_NMS = true;
    // Used for packet mode
    private Object idlePacket;
    private Object idlePacket2;
    // Use for nms
    private Method getHandle;
    private Field connection;
    private Method handleFlying;

    public BukkitViaMovementTransmitter() {
        USE_NMS = Via.getConfig().isNMSPlayerTicking();

        Class<?> idlePacketClass;
        try {
            idlePacketClass = NMSUtil.nms("PacketPlayInFlying");
        } catch (ClassNotFoundException e) {
            return; // We'll hope this is 1.9.4+
        }
        try {
            idlePacket = idlePacketClass.newInstance();
            idlePacket2 = idlePacketClass.newInstance();

            Field flying = idlePacketClass.getDeclaredField("f");
            flying.setAccessible(true);

            flying.set(idlePacket2, true);
        } catch (NoSuchFieldException | InstantiationException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't make player idle packet, help!", e);
        }
        if (USE_NMS) {
            try {
                getHandle = NMSUtil.obc("entity.CraftPlayer").getDeclaredMethod("getHandle");
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find CraftPlayer", e);
            }

            try {
                connection = NMSUtil.nms("EntityPlayer").getDeclaredField("playerConnection");
            } catch (NoSuchFieldException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find Player Connection", e);
            }

            try {
                handleFlying = NMSUtil.nms("PlayerConnection").getDeclaredMethod("a", idlePacketClass);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find CraftPlayer", e);
            }
        }
    }

    public Object getFlyingPacket() {
        if (idlePacket == null) throw new NullPointerException("Could not locate flying packet");

        return idlePacket;
    }

    public Object getGroundPacket() {
        if (idlePacket == null) throw new NullPointerException("Could not locate flying packet");

        return idlePacket2;
    }

    @Override
    public void sendPlayer(UserConnection info) {
        if (USE_NMS) {
            Player player = Bukkit.getPlayer(info.getProtocolInfo().getUuid());
            if (player != null) {
                try {
                    // Tick player
                    Object entityPlayer = getHandle.invoke(player);
                    Object pc = connection.get(entityPlayer);
                    if (pc != null) {
                        handleFlying.invoke(pc, (info.get(MovementTracker.class).isGround() ? idlePacket2 : idlePacket));
                        // Tick world
                        info.get(MovementTracker.class).incrementIdlePacket();
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Via.getPlatform().getLogger().log(Level.WARNING, "Failed to handle idle packet", e);
                }
            }
        } else {
            ChannelHandlerContext context = PipelineUtil.getContextBefore("decoder", info.getChannel().pipeline());
            if (context == null) {
                return;
            }

            info.getChannel().eventLoop().execute(() -> {
                MovementTracker movementTracker = info.get(MovementTracker.class);
                if (movementTracker == null) {
                    return;
                }

                if (movementTracker.isGround()) {
                    context.fireChannelRead(getGroundPacket());
                } else {
                    context.fireChannelRead(getFlyingPacket());
                }
                movementTracker.incrementIdlePacket();
            });
        }
    }
}
