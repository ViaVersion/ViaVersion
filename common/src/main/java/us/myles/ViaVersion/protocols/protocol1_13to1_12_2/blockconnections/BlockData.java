package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class BlockData {
    private final Map<String, boolean[]> connectData = new HashMap<>();

    public void put(String key, boolean[] booleans) {
        connectData.put(key, booleans);
    }

    public boolean connectsTo(String blockConnection, BlockFace face, boolean pre1_12AbstractFence) {
        boolean[] booleans = null;
        if (pre1_12AbstractFence) {
            booleans = connectData.get("allFalseIfStairPre1_12"); // https://minecraft.gamepedia.com/Java_Edition_1.12
        }
        if (booleans == null) {
            booleans = connectData.get(blockConnection);
        }
        return booleans != null && booleans[face.ordinal()];
    }
}
