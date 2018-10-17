package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class NetherFenceConnectionHandler extends AbstractFenceConnectionHandler{

    static void init() {
        List<String> baseFences = new ArrayList<>();
        baseFences.add("minecraft:nether_brick_fence");

        new NetherFenceConnectionHandler("netherFenceConnections", baseFences);
    }

    public NetherFenceConnectionHandler(String blockConnections, List<String> keyList) {
        super(blockConnections, keyList);
    }

    @Override
    public void onConnect(Position position, int blockState, ConnectionData connectionData, WrappedBlockdata blockdata) {}
}
