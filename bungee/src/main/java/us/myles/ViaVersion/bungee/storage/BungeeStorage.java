package us.myles.ViaVersion.bungee.storage;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BungeeStorage extends StoredObject {
    private static Field bossField;

    static {
        try {
            Class<?> user = Class.forName("net.md_5.bungee.UserConnection");
            bossField = user.getDeclaredField("sentBossBars");
            bossField.setAccessible(true);
        } catch (ClassNotFoundException e) {
            // Not supported *shrug* probably modified
        } catch (NoSuchFieldException e) {
            // Not supported, old version probably
        }
    }

    private final ProxiedPlayer player;
    private String currentServer;
    private Set<UUID> bossbar;

    public BungeeStorage(UserConnection user, ProxiedPlayer player) {
        super(user);
        this.player = player;
        this.currentServer = "";

        // Get bossbar list if it's supported
        if (bossField != null) {
            try {
                bossbar = (Set<UUID>) bossField.get(player);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(String currentServer) {
        this.currentServer = currentServer;
    }

    public Set<UUID> getBossbar() {
        return bossbar;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeeStorage that = (BungeeStorage) o;
        if (!Objects.equals(player, that.player)) return false;
        if (!Objects.equals(currentServer, that.currentServer)) return false;
        return Objects.equals(bossbar, that.bossbar);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (currentServer != null ? currentServer.hashCode() : 0);
        result = 31 * result + (bossbar != null ? bossbar.hashCode() : 0);
        return result;
    }
}
