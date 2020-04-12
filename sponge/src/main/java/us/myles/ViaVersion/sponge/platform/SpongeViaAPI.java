package us.myles.ViaVersion.sponge.platform;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class SpongeViaAPI implements ViaAPI<Player> {

    @Override
    public int getPlayerVersion(@NonNull Player player) {
        if (!isPorted(player.getUniqueId()))
            return ProtocolRegistry.SERVER_PROTOCOL;
        return getPortedPlayers().get(player.getUniqueId()).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(@NonNull UUID uuid) {
        if (!isPorted(uuid))
            return ProtocolRegistry.SERVER_PROTOCOL;
        return getPortedPlayers().get(uuid).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public boolean isPorted(UUID playerUUID) {
        return getPortedPlayers().containsKey(playerUUID);
    }

    @Override
    public String getVersion() {
        return Via.getPlatform().getPluginVersion();
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException {
        if (!isPorted(uuid)) throw new IllegalArgumentException("This player is not controlled by ViaVersion!");
        UserConnection ci = getPortedPlayers().get(uuid);
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

    public Map<UUID, UserConnection> getPortedPlayers() {
        return Via.getManager().getConnectedClients();
    }
}
