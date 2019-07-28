package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;

import java.util.HashMap;
import java.util.Map;

public class ChunkLightStorage extends StoredObject {

    private final Map<Long, NibbleArray[]> skyLight = new HashMap<>();

    public ChunkLightStorage(UserConnection user) {
        super(user);
    }

    public Map<Long, NibbleArray[]> getSkyLight() {
        return skyLight;
    }
}
