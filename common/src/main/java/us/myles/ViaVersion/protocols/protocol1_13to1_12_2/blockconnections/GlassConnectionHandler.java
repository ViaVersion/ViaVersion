package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

public class GlassConnectionHandler extends AbstractFenceConnectionHandler{

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

    protected Byte getStates(UserConnection user, Position position, int blockState) {
        byte states = 0;
        if (connects(BlockFace.EAST, getBlockData(user, position.getRelative(BlockFace.EAST)))) states |= 1;
        if (connects(BlockFace.NORTH, getBlockData(user, position.getRelative(BlockFace.NORTH)))) states |= 2;
        if (connects(BlockFace.SOUTH, getBlockData(user, position.getRelative(BlockFace.SOUTH)))) states |= 4;
        if (connects(BlockFace.WEST, getBlockData(user, position.getRelative(BlockFace.WEST)))) states |= 8;
        return states == 0 && (ProtocolRegistry.SERVER_PROTOCOL <= 47 && ProtocolRegistry.SERVER_PROTOCOL != -1) ? 15 : states;
    }
}
