package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class BasicFenceConnectionHandler extends AbstractFenceConnectionHandler{

    static void init() {
        List<String> baseFences = new ArrayList<>();
        baseFences.add("minecraft:oak_fence");
        baseFences.add("minecraft:birch_fence");
        baseFences.add("minecraft:jungle_fence");
        baseFences.add("minecraft:dark_oak_fence");
        baseFences.add("minecraft:acacia_fence");
        baseFences.add("minecraft:spruce_fence");
//        baseFences.add("minecraft:nether_brick_fence");
//        baseFences.add("minecraft:cobblestone_wall");
//        baseFences.add("minecraft:mossy_cobblestone_wall");

        new BasicFenceConnectionHandler("fenceConnections", baseFences);
    }

    public BasicFenceConnectionHandler(String blockConnections, List<String> keyList) {
        super(blockConnections, keyList);
    }

    @Override
    public void onConnect(Position position, int blockState, ConnectionData connectionData, WrappedBlockdata blockdata) {

    }
}
