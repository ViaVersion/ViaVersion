package us.myles.ViaVersion.bungee.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

public class BungeeBlockConnectionData extends StoredObject {
    private Map<Position, Integer> blockStorage = new HashMap<>();

    public BungeeBlockConnectionData(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        blockStorage.put(position, blockState);
    }

    public int get(Position position) {
        return blockStorage.containsKey(position) ? blockStorage.get(position) : 0;
    }

    public void remove(Position position) {
        blockStorage.remove(position);
    }
}
