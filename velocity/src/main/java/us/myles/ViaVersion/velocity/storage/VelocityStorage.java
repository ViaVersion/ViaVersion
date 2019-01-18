package us.myles.ViaVersion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class VelocityStorage extends StoredObject {
    private Player player;
    private String currentServer;
    private List<UUID> bossbar;

    public VelocityStorage(UserConnection user, Player player) {
        super(user);
        this.player = player;
        this.currentServer = "";
    }

    public void saveServerBossBars() {
        // Get bossbar list if it's supported
        try {
            Object connection = ReflectionUtil.invoke(player, "getMinecraftConnection");
            Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
            if (sessionHandler.getClass().getSimpleName().contains("Play")) {
                bossbar = (List<UUID>) ReflectionUtil.invoke(sessionHandler, "getServerBossBars");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
