package us.myles.ViaVersion.boss;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossFlag;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

import java.util.*;

@Getter
public abstract class CommonBoss<T> extends BossBar<T> {
    private final UUID uuid;
    private String title;
    private float health;
    private BossColor color;
    private BossStyle style;
    private final Set<UUID> players;
    private boolean visible;
    private final Set<BossFlag> flags;

    public CommonBoss(String title, float health, BossColor color, BossStyle style) {
        Preconditions.checkNotNull(title, "Title cannot be null");
        Preconditions.checkArgument((health >= 0 && health <= 1), "Health must be between 0 and 1");

        this.uuid = UUID.randomUUID();
        this.title = title;
        this.health = health;
        this.color = color == null ? BossColor.PURPLE : color;
        this.style = style == null ? BossStyle.SOLID : style;
        this.players = new HashSet<>();
        this.flags = new HashSet<>();
        visible = true;
    }

    @Override
    public BossBar setTitle(@NonNull String title) {
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
    public BossBar setColor(@NonNull BossColor color) {
        this.color = color;
        sendPacket(CommonBoss.UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar setStyle(@NonNull BossStyle style) {
        this.style = style;
        sendPacket(CommonBoss.UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar addPlayer(UUID player) {
        if (!players.contains(player)) {
            players.add(player);
            if (visible) {
                UserConnection user = Via.getManager().getConnection(player);
                sendPacket(player, getPacket(CommonBoss.UpdateAction.ADD, user));
            }
        }
        return this;
    }

    @Override
    public BossBar removePlayer(UUID uuid) {
        if (players.contains(uuid)) {
            players.remove(uuid);
            UserConnection user = Via.getManager().getConnection(uuid);
            sendPacket(uuid, getPacket(UpdateAction.REMOVE, user));
        }
        return this;
    }

    @Override
    public BossBar addFlag(@NonNull BossFlag flag) {
        if (!hasFlag(flag))
            flags.add(flag);
        sendPacket(CommonBoss.UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public BossBar removeFlag(@NonNull BossFlag flag) {
        if (hasFlag(flag))
            flags.remove(flag);
        sendPacket(CommonBoss.UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public boolean hasFlag(@NonNull BossFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
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

    @Override
    public UUID getId() {
        return uuid;
    }

    private void setVisible(boolean value) {
        if (visible != value) {
            visible = value;
            sendPacket(value ? CommonBoss.UpdateAction.ADD : CommonBoss.UpdateAction.REMOVE);
        }
    }

    private void sendPacket(UpdateAction action) {
        for (UUID uuid : new ArrayList<>(players)) {
            UserConnection connection = Via.getManager().getConnection(uuid);
            PacketWrapper wrapper = getPacket(action, connection);
            sendPacket(uuid, wrapper);
        }
    }

    private void sendPacket(UUID uuid, PacketWrapper wrapper) {
        if (!Via.getAPI().isPorted(uuid) || !(Via.getAPI().getPlayerVersion(uuid) >= ProtocolVersion.v1_9.getId())) {
            players.remove(uuid);
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
