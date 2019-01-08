package us.myles.ViaVersion.velocity.platform;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import us.myles.ViaVersion.VelocityPlugin;
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

public class VelocityViaAPI implements ViaAPI<Player> {
    @Override
    public int getPlayerVersion(@NonNull Player player) {
        if (!isPorted(player.getUniqueId()))
            return player.getProtocolVersion().getProtocol();
        return getPortedPlayers().get(player.getUniqueId()).get(ProtocolInfo.class).getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(@NonNull UUID uuid) {
        if (!isPorted(uuid)) {
            return VelocityPlugin.PROXY.getPlayer(uuid)
                    .map(InboundConnection::getProtocolVersion)
                    .map(ProtocolVersion::getProtocol)
                    .orElse(ProtocolRegistry.SERVER_PROTOCOL);
        }
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
        return new VelocityBossBar(title, 1F, color, style);
    }

    @Override
    public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new VelocityBossBar(title, health, color, style);
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        SortedSet<Integer> outputSet = new TreeSet<>(ProtocolRegistry.getSupportedVersions());
        outputSet.removeAll(Via.getPlatform().getConf().getBlockedProtocols());

        return outputSet;
    }

    public Map<UUID, UserConnection> getPortedPlayers() {
        return Via.getManager().getPortedPlayers();
    }
}
