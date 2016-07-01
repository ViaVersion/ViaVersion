package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage;

import lombok.Getter;
import org.bukkit.World;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Getter
public class ClientWorld extends StoredObject {
    private World.Environment environment;

    public ClientWorld(UserConnection user) {
        super(user);
    }

    public void setEnvironment(int environmentId) {
        this.environment = getEnvFromId(environmentId);
    }

    private World.Environment getEnvFromId(int id) {
        switch(id) {
            case -1:
                return World.Environment.NETHER;
            case 0:
                return World.Environment.NORMAL;
            case 1:
                return World.Environment.THE_END;
            default:
                throw new IllegalArgumentException("Invalid environment id:" + id);
        }
    }
}
