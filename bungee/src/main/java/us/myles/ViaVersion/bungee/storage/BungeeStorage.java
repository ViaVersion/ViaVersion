package us.myles.ViaVersion.bungee.storage;

import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
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
}
