package us.myles.ViaVersion.bungee.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Data
@EqualsAndHashCode(callSuper = true)
public class BungeeStorage extends StoredObject {
    private ProxiedPlayer player;
    private String currentServer;

    public BungeeStorage(UserConnection user, ProxiedPlayer player) {
        super(user);
        this.player = player;
        this.currentServer = "";
    }
}
