package us.myles.ViaVersion.boss;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossFlag;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.transformers.OutgoingTransformer;
import us.myles.ViaVersion.util.PacketUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    public void setTitle(String title) {
        Validate.notNull(title, "Title cannot be null");
        this.title = title;
        sendPacket(UpdateAction.UPDATE_TITLE);
    }

    @Override
    public void setHealth(float health) {
        Validate.isTrue((health >= 0 && health <= 1), "Health must be between 0 and 1");
        this.health = health;
        sendPacket(UpdateAction.UPDATE_HEALTH);
    }

    @Override
    public BossColor getColor() {
        return color;
    }

    @Override
    public void setColor(BossColor color) {
        Validate.notNull(color, "Color cannot be null");
        this.color = color;
        sendPacket(UpdateAction.UPDATE_STYLE);
    }

    @Override
    public void setStyle(BossStyle style) {
        Validate.notNull(style, "Style cannot be null");
        this.style = style;
        sendPacket(UpdateAction.UPDATE_STYLE);
    }

    @Override
    public void addPlayer(Player player) {
        if (player != null && !players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            if (visible)
                sendPacket(player.getUniqueId(), getPacket(UpdateAction.ADD));
        }
    }

    @Override
    public void removePlayer(Player player) {
        if (player != null && players.contains(player.getUniqueId())) {
            players.remove(player.getUniqueId());
            sendPacket(player.getUniqueId(), getPacket(UpdateAction.REMOVE));
        }
    }

    @Override
    public void addFlag(BossFlag flag) {
        if (!hasFlag(flag))
            flags.add(flag);
        sendPacket(UpdateAction.UPDATE_FLAGS);
    }

    @Override
    public void removeFlag(BossFlag flag) {
        if (hasFlag(flag))
            flags.remove(flag);
        sendPacket(UpdateAction.UPDATE_FLAGS);
    }

    @Override
    public boolean hasFlag(BossFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void show() {
        setVisible(true);
    }

    @Override
    public void hide() {
        setVisible(false);
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
        ByteBuf buf = getPacket(action);
        for (UUID uuid : players)
            sendPacket(uuid, buf);
    }

    private void sendPacket(UUID uuid, ByteBuf buf) {
        if (!ViaVersion.getInstance().isPorted(uuid)) {
            players.remove(uuid);
            return;
        }
        ViaVersion.getInstance().sendRawPacket(uuid, buf);
    }

    private ByteBuf getPacket(UpdateAction action) {
        ByteBuf buf = Unpooled.buffer();
        PacketUtil.writeVarInt(PacketType.PLAY_BOSS_BAR.getNewPacketID(), buf);
        PacketUtil.writeUUID(uuid, buf);
        PacketUtil.writeVarInt(action.getId(), buf);
        switch (action) {
            case ADD:
                PacketUtil.writeString(fixJson(title), buf);
                buf.writeFloat(health);
                PacketUtil.writeVarInt(color.getId(), buf);
                PacketUtil.writeVarInt(style.getId(), buf);
                buf.writeByte(flagToBytes());
                break;
            case REMOVE:
                break;
            case UPDATE_HEALTH:
                buf.writeFloat(health);
                break;
            case UPDATE_TITLE:
                PacketUtil.writeString(fixJson(title), buf);
                break;
            case UPDATE_STYLE:
                PacketUtil.writeVarInt(color.getId(), buf);
                PacketUtil.writeVarInt(style.getId(), buf);
                break;
            case UPDATE_FLAGS:
                buf.writeByte(flagToBytes());
                break;
        }

        return buf;
    }

    private int flagToBytes() {
        int bitmask = 0;
        for (BossFlag flag : flags)
            bitmask |= flag.getId();
        return bitmask;
    }

    private String fixJson(String text) {
        return OutgoingTransformer.fixJson(text);
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
