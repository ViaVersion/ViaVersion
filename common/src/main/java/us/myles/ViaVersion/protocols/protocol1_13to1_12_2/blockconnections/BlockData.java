package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class BlockData {

    private Map<String, Boolean[]> connectData = new HashMap<>();

    public void put(String key, Boolean[] booleans){
        connectData.put(key, booleans);
    }

    public boolean connectTo(String blockConnection, BlockFace face){
        if(!connectData.containsKey(blockConnection)) return false;
        return connectData.get(blockConnection)[face.ordinal()];
    }
}
