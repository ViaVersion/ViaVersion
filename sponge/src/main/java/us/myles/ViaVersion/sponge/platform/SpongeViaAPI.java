package us.myles.ViaVersion.sponge.platform;

import io.netty.buffer.ByteBuf;
import org.spongepowered.api.entity.living.player.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class SpongeViaAPI implements ViaAPI<Player> {

    @Override
    public int getPlayerVersion(Player player) {
        return getPlayerVersion(player.getUniqueId());
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        UserConnection connection = Via.getManager().getConnection(uuid);
        return connection != null ? connection.getProtocolInfo().getProtocolVersion() : -1;
    }

    @Override
    public boolean isInjected(UUID playerUUID) {
        return Via.getManager().isClientConnected(playerUUID);
    }

    @Override
    public String getVersion() {
        return Via.getPlatform().getPluginVersion();
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException {
        if (!isInjected(uuid)) throw new IllegalArgumentException("This player is not controlled by ViaVersion!");
        UserConnection ci = Via.getManager().getConnection(uuid);
        ci.sendRawPacket(packet);
    }

    @Override
    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        sendRawPacket(player.getUniqueId(), packet);
    }

    @Override
    public BossBar createBossBar(String title, BossColor color, BossStyle style) {
        return new SpongeBossBar(title, 1F, color, style);
    }

    @Override
    public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new SpongeBossBar(title, health, color, style);
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        SortedSet<Integer> outputSet = new TreeSet<>(ProtocolRegistry.getSupportedVersions());
        outputSet.removeAll(Via.getPlatform().getConf().getBlockedProtocols());

        return outputSet;
    }
}
