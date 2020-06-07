package us.myles.ViaVersion.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.VelocityPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class VelocityViaAPI implements ViaAPI<Player> {
    @Override
    public int getPlayerVersion(Player player) {
        if (!isInjected(player.getUniqueId()))
            return player.getProtocolVersion().getProtocol();
        return Via.getManager().getConnection(player.getUniqueId()).getProtocolInfo().getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        return getPlayerVersion(VelocityPlugin.PROXY.getPlayer(uuid).orElseThrow(NoSuchElementException::new));

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

}
