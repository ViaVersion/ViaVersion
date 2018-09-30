package us.myles.ViaVersion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
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
        try {
            Object connection = ReflectionUtil.invoke(player, "getConnection");
            Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
            if (sessionHandler.getClass().getSimpleName().contains("Play")) {
                bossbar = (Set<UUID>) ReflectionUtil.invoke(sessionHandler, "getServerBossBars");
                // TODO make this work
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
