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
package us.myles.ViaVersion.boss;

import com.google.common.base.Preconditions;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossFlag;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommonBoss<T> extends BossBar<T> {
    private final UUID uuid;
    private final Set<UserConnection> connections;
    private final Set<BossFlag> flags;
    private String title;
    private float health;
    private BossColor color;
    private BossStyle style;
    private boolean visible;

    public CommonBoss(String title, float health, BossColor color, BossStyle style) {
        Preconditions.checkNotNull(title, "Title cannot be null");
        Preconditions.checkArgument((health >= 0 && health <= 1), "Health must be between 0 and 1");

        this.uuid = UUID.randomUUID();
        this.title = title;
        this.health = health;
        this.color = color == null ? BossColor.PURPLE : color;
        this.style = style == null ? BossStyle.SOLID : style;
        this.connections = Collections.newSetFromMap(new WeakHashMap<>());
        this.flags = new HashSet<>();
        visible = true;
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
        Preconditions.checkArgument((health >= 0 && health <= 1), "Health must be between 0 and 1");
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
        return addConnection(Via.getManager().getConnectionManager().getConnectedClient(player));
    }

    @Override
    public BossBar addConnection(UserConnection conn) {
        if (connections.add(conn) && visible) {
            sendPacketConnection(conn, getPacket(CommonBoss.UpdateAction.ADD, conn));
        }
        return this;
    }

    @Override
    public BossBar removePlayer(UUID uuid) {
        return removeConnection(Via.getManager().getConnectionManager().getConnectedClient(uuid));
    }

    @Override
    public BossBar removeConnection(UserConnection conn) {
        if (connections.remove(conn)) {
            sendPacketConnection(conn, getPacket(UpdateAction.REMOVE, conn));
        }
        return this;
    }

    @Override
    public BossBar addFlag(BossFlag flag) {
        Preconditions.checkNotNull(flag);
        if (!hasFlag(flag))
            flags.add(flag);
        sendPacket(CommonBoss.UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public BossBar removeFlag(BossFlag flag) {
        Preconditions.checkNotNull(flag);
        if (hasFlag(flag))
            flags.remove(flag);
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
        return connections.stream().map(conn -> Via.getManager().getConnectionManager().getConnectedClientId(conn)).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UserConnection> getConnections() {
        return Collections.unmodifiableSet(connections);
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
        for (UserConnection conn : new ArrayList<>(connections)) {
            PacketWrapper wrapper = getPacket(action, conn);
            sendPacketConnection(conn, wrapper);
        }
    }

    private void sendPacketConnection(UserConnection conn, PacketWrapper wrapper) {
        if (conn.getProtocolInfo() == null || !conn.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
            connections.remove(conn);
            return;
        }
        try {
            wrapper.send(Protocol1_9To1_8.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PacketWrapper getPacket(UpdateAction action, UserConnection connection) {
        try {
            PacketWrapper wrapper = new PacketWrapper(0x0C, null, connection); // TODO don't use fixed packet ids for future support
            wrapper.write(Type.UUID, uuid);
            wrapper.write(Type.VAR_INT, action.getId());
            switch (action) {
                case ADD:
                    Protocol1_9To1_8.FIX_JSON.write(wrapper, title);
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
                    Protocol1_9To1_8.FIX_JSON.write(wrapper, title);
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
            e.printStackTrace();
        }
        return null;
    }

    private int flagToBytes() {
        int bitmask = 0;
        for (BossFlag flag : flags)
            bitmask |= flag.getId();
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
