package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class BlockData {
    private Map<String, Boolean[]> connectData = new HashMap<>();

    public void put(String key, Boolean[] booleans) {
        connectData.put(key, booleans);
    }

    public boolean connectsTo(String blockConnection, BlockFace face) {
        final Boolean[] booleans = connectData.get(blockConnection);
        return booleans != null && booleans[face.ordinal()];
    }
}
