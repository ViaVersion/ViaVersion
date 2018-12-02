package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

public class BasicFenceConnectionHandler extends AbstractFenceConnectionHandler {

    static void init() {
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:oak_fence");
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:birch_fence");
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:jungle_fence");
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:dark_oak_fence");
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:acacia_fence");
        new BasicFenceConnectionHandler("fenceConnections", "minecraft:spruce_fence");
    }

    public BasicFenceConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
    }
}
