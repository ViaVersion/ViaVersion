package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

public class GlassConnectionHandler extends AbstractFenceConnectionHandler {

    static void init() {
        new GlassConnectionHandler("paneConnections", "minecraft:white_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:orange_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:magenta_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:light_blue_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:yellow_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:lime_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:pink_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:gray_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:light_gray_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:cyan_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:purple_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:blue_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:brown_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:green_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:red_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:black_stained_glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:glass_pane");
        new GlassConnectionHandler("paneConnections", "minecraft:iron_bars");
    }

    public GlassConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        return states == 0
                && user.get(ProtocolInfo.class).getServerProtocolVersion() <= 47
                && user.get(ProtocolInfo.class).getServerProtocolVersion() != -1 ? 0xF : states;
    }
}
