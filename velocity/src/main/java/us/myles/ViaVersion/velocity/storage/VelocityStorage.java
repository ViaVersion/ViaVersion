package us.myles.ViaVersion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import lombok.EqualsAndHashCode;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class VelocityStorage extends StoredObject {
    private final Player player;
    private String currentServer;
    private List<UUID> cachedBossbar;
    private static Method getServerBossBars;
    private static Class<?> clientPlaySessionHandler;
    private static Method getMinecraftConnection;

    static {
        try {
            clientPlaySessionHandler = Class.forName("com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler");
            getServerBossBars = clientPlaySessionHandler
                    .getDeclaredMethod("getServerBossBars");
            getMinecraftConnection = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer")
                    .getDeclaredMethod("getMinecraftConnection");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public VelocityStorage(UserConnection user, Player player) {
        super(user);
        this.player = player;
        this.currentServer = "";
    }

    public List<UUID> getBossbar() {
        if (cachedBossbar == null) {
            if (clientPlaySessionHandler == null) return null;
            if (getServerBossBars == null) return null;
            if (getMinecraftConnection == null) return null;
            // Get bossbar list if it's supported
            try {
                Object connection = getMinecraftConnection.invoke(player);
                Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
                if (clientPlaySessionHandler.isInstance(sessionHandler)) {
                    cachedBossbar = (List<UUID>) getServerBossBars.invoke(sessionHandler);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return cachedBossbar;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(final String currentServer) {
        this.currentServer = currentServer;
    }

    public List<UUID> getCachedBossbar() {
        return cachedBossbar;
    }
}
