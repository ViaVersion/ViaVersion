package us.myles.ViaVersion.bukkit.platform;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.boss.ViaBossBar;
import us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class BukkitViaAPI implements ViaAPI<Player> {
    private final ViaVersionPlugin plugin;

    private final Set<UUID> disabledShieldBlocking = Sets.newConcurrentHashSet();

    public BukkitViaAPI(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getPlayerVersion(Player player) {
        return getPlayerVersion(player.getUniqueId());
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        if (!isInjected(uuid))
            return getExternalVersion(Bukkit.getPlayer(uuid));
        return Via.getManager().getConnection(uuid).getProtocolInfo().getProtocolVersion();
    }

    private int getExternalVersion(Player player) {
        if (!isProtocolSupport()) {
            return ProtocolRegistry.SERVER_PROTOCOL;
        } else {
            return ProtocolSupportUtil.getProtocolVersion(player);
        }
    }

    @Override
    public boolean isInjected(UUID playerUUID) {
        return Via.getManager().isClientConnected(playerUUID);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
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
    public BossBar<Player> createBossBar(String title, BossColor color, BossStyle style) {
        return new ViaBossBar(title, 1F, color, style);
    }

    @Override
    public BossBar<Player> createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new ViaBossBar(title, health, color, style);
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        SortedSet<Integer> outputSet = new TreeSet<>(ProtocolRegistry.getSupportedVersions());
        outputSet.removeAll(Via.getPlatform().getConf().getBlockedProtocols());

        return outputSet;
    }

    @Override
    public boolean isShieldBlockingDisabled(UUID uuid) {
        return disabledShieldBlocking.contains(uuid);
    }

    @Override
    public void disableShieldBlocking(UUID uuid) {
        disabledShieldBlocking.add(uuid);
    }

    @Override
    public void enableShieldBlocking(UUID uuid) {
        disabledShieldBlocking.remove(uuid);
    }

    /**
     * Returns if this version is a compatibility build for spigot.
     * Eg. 1.9.1 / 1.9.2 allow certain versions to connect
     *
     * @return true if compat Spigot build
     */
    public boolean isCompatSpigotBuild() {
        return plugin.isCompatSpigotBuild();
    }

    /**
     * Returns if ProtocolSupport is also being used.
     *
     * @return true if ProtocolSupport is used
     */
    public boolean isProtocolSupport() {
        return plugin.isProtocolSupport();
    }

}
