package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FlowerConnectionHandler extends ConnectionHandler{

    private static Set<String> baseFlower = new HashSet<>();

    static void init(){
        baseFlower.add("minecraft:rose_bush");
        baseFlower.add("minecraft:sunflower");
        baseFlower.add("minecraft:peony");
        baseFlower.add("minecraft:tall_grass");
        baseFlower.add("minecraft:large_fern");
        baseFlower.add("minecraft:lilac");


        FlowerConnectionHandler handler = new FlowerConnectionHandler();

        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            String key = blockState.getKey().split("\\[")[0];
            if (baseFlower.contains(key)) {
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), handler);
            }
        }
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        int blockBelowId = getBlockData(user, position.getRelative(BlockFace.BOTTOM));
        if(canConnect(blockBelowId)){
            WrappedBlockData blockData = WrappedBlockData.fromStateId(blockBelowId);
            blockData.set("half", "upper");
            return blockData.getBlockStateId();
        }
        return blockState;
    }
}
