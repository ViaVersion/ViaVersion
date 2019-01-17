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
    }
}
