package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

public class NetherFenceConnectionHandler extends AbstractFenceConnectionHandler {

    static void init() {
        new NetherFenceConnectionHandler("netherFenceConnections", "minecraft:nether_brick_fence");
    }

    public NetherFenceConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
    }
}
