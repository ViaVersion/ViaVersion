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
package com.viaversion.viaversion.legacy.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossFlag;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class CommonBoss implements BossBar {
    private final UUID uuid;
    private final Map<UUID, UserConnection> connections;
    private final Set<BossFlag> flags;
    private String title;
    private float health;
    private BossColor color;
    private BossStyle style;
    private boolean visible;

    public CommonBoss(String title, float health, BossColor color, BossStyle style) {
        Preconditions.checkNotNull(title, "Title cannot be null");
        Preconditions.checkArgument((health >= 0 && health <= 1), "Health must be between 0 and 1. Input: " + health);

        this.uuid = UUID.randomUUID();
        this.title = title;
        this.health = health;
        this.color = color == null ? BossColor.PURPLE : color;
        this.style = style == null ? BossStyle.SOLID : style;
        this.connections = new MapMaker().weakValues().makeMap();
        this.flags = EnumSet.noneOf(BossFlag.class);
        this.visible = true;
    }

    @Override
    public BossBar setTitle(String title) {
        Preconditions.checkNotNull(title);
        this.title = title;
        sendPacket(CommonBoss.UpdateAction.UPDATE_TITLE);
        return this;
    }

    @Override
    public BossBar setHealth(float health) {
        Preconditions.checkArgument((health >= 0 && health <= 1), "Health must be between 0 and 1. Input: " + health);
        this.health = health;
        sendPacket(CommonBoss.UpdateAction.UPDATE_HEALTH);
        return this;
    }

    @Override
    public BossColor getColor() {
        return color;
    }

    @Override
    public BossBar setColor(BossColor color) {
        Preconditions.checkNotNull(color);
        this.color = color;
        sendPacket(CommonBoss.UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar setStyle(BossStyle style) {
        Preconditions.checkNotNull(style);
        this.style = style;
        sendPacket(CommonBoss.UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar addPlayer(UUID player) {
        UserConnection client = Via.getManager().getConnectionManager().getConnectedClient(player);
        if (client != null) {
            addConnection(client);
        }
        return this;
    }

    @Override
    public BossBar addConnection(UserConnection conn) {
        if (connections.put(conn.getProtocolInfo().getUuid(), conn) == null && visible) {
            sendPacketConnection(conn, getPacket(CommonBoss.UpdateAction.ADD, conn));
        }
        return this;
    }

    @Override
    public BossBar removePlayer(UUID uuid) {
        UserConnection client = connections.remove(uuid);
        if (client != null) {
            sendPacketConnection(client, getPacket(UpdateAction.REMOVE, client));
        }
        return this;
    }

    @Override
    public BossBar removeConnection(UserConnection conn) {
        removePlayer(conn.getProtocolInfo().getUuid());
        return this;
    }

    @Override
    public BossBar addFlag(BossFlag flag) {
        Preconditions.checkNotNull(flag);
        if (!hasFlag(flag)) {
            flags.add(flag);
        }
        sendPacket(CommonBoss.UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public BossBar removeFlag(BossFlag flag) {
        Preconditions.checkNotNull(flag);
        if (hasFlag(flag)) {
            flags.remove(flag);
        }
        sendPacket(CommonBoss.UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public boolean hasFlag(BossFlag flag) {
        Preconditions.checkNotNull(flag);
        return flags.contains(flag);
    }

    @Override
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(connections.keySet());
    }

    @Override
    public Set<UserConnection> getConnections() {
        return Collections.unmodifiableSet(new HashSet<>(connections.values()));
    }

    @Override
    public BossBar show() {
        setVisible(true);
        return this;
    }

    @Override
    public BossBar hide() {
        setVisible(false);
        return this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    private void setVisible(boolean value) {
        if (visible != value) {
            visible = value;
            sendPacket(value ? CommonBoss.UpdateAction.ADD : CommonBoss.UpdateAction.REMOVE);
        }
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public BossStyle getStyle() {
        return style;
    }

    public Set<BossFlag> getFlags() {
        return flags;
    }

    private void sendPacket(UpdateAction action) {
        for (UserConnection conn : new ArrayList<>(connections.values())) {
            PacketWrapper wrapper = getPacket(action, conn);
            sendPacketConnection(conn, wrapper);
        }
    }

    private void sendPacketConnection(UserConnection conn, PacketWrapper wrapper) {
        if (conn.getProtocolInfo() == null || !conn.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
            connections.remove(conn.getProtocolInfo().getUuid());
            return;
        }
        try {
            wrapper.scheduleSend(Protocol1_9To1_8.class);
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send bossbar packet", e);
        }
    }

    private PacketWrapper getPacket(UpdateAction action, UserConnection connection) {
        try {
            PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, null, connection);
            wrapper.write(Type.UUID, uuid);
            wrapper.write(Type.VAR_INT, action.getId());
            switch (action) {
                case ADD:
                    wrapper.write(Type.COMPONENT, ComponentUtil.plainToJson(title));
                    wrapper.write(Type.FLOAT, health);
                    wrapper.write(Type.VAR_INT, color.getId());
                    wrapper.write(Type.VAR_INT, style.getId());
                    wrapper.write(Type.BYTE, (byte) flagToBytes());
                    break;
                case REMOVE:
                    break;
                case UPDATE_HEALTH:
                    wrapper.write(Type.FLOAT, health);
                    break;
                case UPDATE_TITLE:
                    wrapper.write(Type.COMPONENT, ComponentUtil.plainToJson(title));
                    break;
                case UPDATE_STYLE:
                    wrapper.write(Type.VAR_INT, color.getId());
                    wrapper.write(Type.VAR_INT, style.getId());
                    break;
                case UPDATE_FLAGS:
                    wrapper.write(Type.BYTE, (byte) flagToBytes());
                    break;
            }

            return wrapper;
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to create bossbar packet", e);
        }
        return null;
    }

    private int flagToBytes() {
        int bitmask = 0;
        for (BossFlag flag : flags) {
            bitmask |= flag.getId();
        }
        return bitmask;
    }

    private enum UpdateAction {

        ADD(0),
        REMOVE(1),
        UPDATE_HEALTH(2),
        UPDATE_TITLE(3),
        UPDATE_STYLE(4),
        UPDATE_FLAGS(5);

        private final int id;

        UpdateAction(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
