package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Environment;

/**
 * Stored up until 1.14 to be used in chunk sending.
 */
public class ClientWorld extends StoredObject {
    private Environment environment;

    public ClientWorld(UserConnection user) {
        super(user);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(int environmentId) {
        this.environment = Environment.getEnvironmentById(environmentId);
    }
}
