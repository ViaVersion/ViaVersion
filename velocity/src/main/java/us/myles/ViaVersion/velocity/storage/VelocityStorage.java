package us.myles.ViaVersion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class VelocityStorage extends StoredObject {
    private Player player;
    private String currentServer;
    private Set<UUID> bossbar;

    public VelocityStorage(UserConnection user, Player player) {
        super(user);
        this.player = player;
        this.currentServer = "";

        // Get bossbar list if it's supported
        /* TODO make this work - do we need this?
        try {
            Object connection = ReflectionUtil.invoke(player, "getConnection");
            Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
            if (sessionHandler.getClass().getSimpleName().contains("Play")) {
               bossbar = (Set<UUID>) ReflectionUtil.invoke(sessionHandler, "getServerBossBars");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        */
    }
}
