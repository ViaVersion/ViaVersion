package us.myles.ViaVersion.boss;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossFlag;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.*;

@Getter
public class ViaBossBar implements BossBar {
    private UUID uuid;
    private String title;
    private float health;
    private BossColor color;
    private BossStyle style;
    private Set<UUID> players;
    private boolean visible;
    private Set<BossFlag> flags;

    public ViaBossBar(String title, float health, BossColor color, BossStyle style) {
        Validate.notNull(title, "Title cannot be null");
        Validate.isTrue((health >= 0 && health <= 1), "Health must be between 0 and 1");
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
        sendPacket(UpdateAction.UPDATE_TITLE);
        return this;
    }

    @Override
    public BossBar setHealth(float health) {
        Validate.isTrue((health >= 0 && health <= 1), "Health must be between 0 and 1");
        this.health = health;
        sendPacket(UpdateAction.UPDATE_HEALTH);
        return this;
    }

    @Override
    public BossColor getColor() {
        return color;
    }

    @Override
    public BossBar setColor(@NonNull BossColor color) {
        this.color = color;
        sendPacket(UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar setStyle(@NonNull BossStyle style) {
        this.style = style;
        sendPacket(UpdateAction.UPDATE_STYLE);
        return this;
    }

    @Override
    public BossBar addPlayer(@NonNull Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            if (visible)
                sendPacket(player.getUniqueId(), getPacket(UpdateAction.ADD));
        }
        return this;
    }

    @Override
    public BossBar removePlayer(@NonNull Player player) {
        if (players.contains(player.getUniqueId())) {
            players.remove(player.getUniqueId());
            sendPacket(player.getUniqueId(), getPacket(UpdateAction.REMOVE));
        }
        return this;
    }

    @Override
    public BossBar addPlayers(@NonNull Player... players) {
        for (Player p : players)
            addPlayer(p);
        return this;
    }

    @Override
    public BossBar addFlag(@NonNull BossFlag flag) {
        if (!hasFlag(flag))
            flags.add(flag);
        sendPacket(UpdateAction.UPDATE_FLAGS);
        return this;
    }

    @Override
    public BossBar removeFlag(@NonNull BossFlag flag) {
        if (hasFlag(flag))
            flags.remove(flag);
        sendPacket(UpdateAction.UPDATE_FLAGS);
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

    private void setVisible(boolean value) {
        if (visible != value) {
            visible = value;
            sendPacket(value ? UpdateAction.ADD : UpdateAction.REMOVE);
        }
    }

    private void sendPacket(UpdateAction action) {
        for (UUID uuid : new ArrayList<>(players)) {
            ByteBuf buf = getPacket(action);
            sendPacket(uuid, buf);
        }
    }

    private void sendPacket(UUID uuid, ByteBuf buf) {
        if (!ViaVersion.getInstance().isPorted(uuid) || !(ViaVersion.getInstance().getPlayerVersion(uuid) >= ProtocolVersion.v1_9.getId())) {
            players.remove(uuid);
            buf.release();
            return;
        }
        ViaVersion.getInstance().sendRawPacket(uuid, buf);
    }

    private ByteBuf getPacket(UpdateAction action) {
        try {
            ByteBuf buf = Unpooled.buffer();
            Type.VAR_INT.write(buf, 0x0C); // Boss bar packet
            Type.UUID.write(buf, uuid);
            Type.VAR_INT.write(buf, action.getId());
            switch (action) {
                case ADD:
                    Type.STRING.write(buf, fixJson(title));
                    buf.writeFloat(health);
                    Type.VAR_INT.write(buf, color.getId());
                    Type.VAR_INT.write(buf, style.getId());
                    buf.writeByte(flagToBytes());
                    break;
                case REMOVE:
                    break;
                case UPDATE_HEALTH:
                    buf.writeFloat(health);
                    break;
                case UPDATE_TITLE:
                    Type.STRING.write(buf, fixJson(title));
                    break;
                case UPDATE_STYLE:
                    Type.VAR_INT.write(buf, color.getId());
                    Type.VAR_INT.write(buf, style.getId());
                    break;
                case UPDATE_FLAGS:
                    buf.writeByte(flagToBytes());
                    break;
            }

            return buf;
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

    private String fixJson(String text) {
        return Protocol1_9TO1_8.fixJson(text);
    }

    @RequiredArgsConstructor
    @Getter
    private enum UpdateAction {
        ADD(0),
        REMOVE(1),
        UPDATE_HEALTH(2),
        UPDATE_TITLE(3),
        UPDATE_STYLE(4),
        UPDATE_FLAGS(5);

        private final int id;
    }
}
