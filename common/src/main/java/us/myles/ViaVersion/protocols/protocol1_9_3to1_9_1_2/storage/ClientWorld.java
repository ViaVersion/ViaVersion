package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Environment;

public class ClientWorld extends StoredObject {
    private Environment environment;

    public ClientWorld(UserConnection user) {
        super(user);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(int environmentId) {
        this.environment = getEnvFromId(environmentId);
    }

    private Environment getEnvFromId(int id) {
        Environment output = Environment.getEnvironmentById(id);
        if (output == null) return Environment.NETHER;
        return output;
    }
}
