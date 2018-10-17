package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.Position;

import java.util.*;

public class GlassConnectionHandler extends AbstractFenceConnectionHandler{

    static void init() {
        List<String> basePanes = new ArrayList<>();
        basePanes.add("minecraft:white_stained_glass_pane");
        basePanes.add("minecraft:orange_stained_glass_pane");
        basePanes.add("minecraft:magenta_stained_glass_pane");
        basePanes.add("minecraft:light_blue_stained_glass_pane");
        basePanes.add("minecraft:yellow_stained_glass_pane");
        basePanes.add("minecraft:lime_stained_glass_pane");

        basePanes.add("minecraft:pink_stained_glass_pane");
        basePanes.add("minecraft:gray_stained_glass_pane");
        basePanes.add("minecraft:light_gray_stained_glass_pane");
        basePanes.add("minecraft:cyan_stained_glass_pane");
        basePanes.add("minecraft:purple_stained_glass_pane");
        basePanes.add("minecraft:blue_stained_glass_pane");

        basePanes.add("minecraft:brown_stained_glass_pane");
        basePanes.add("minecraft:green_stained_glass_pane");
        basePanes.add("minecraft:red_stained_glass_pane");
        basePanes.add("minecraft:black_stained_glass_pane");
        basePanes.add("minecraft:glass_pane");
        basePanes.add("minecraft:iron_bars");

        new GlassConnectionHandler("paneConnections", basePanes);
    }

    public GlassConnectionHandler(String blockConnections, List<String> keyList) {
        super(blockConnections, keyList);
    }

    @Override
    public void onConnect(Position position, int blockState, ConnectionData connectionData, WrappedBlockdata blockdata) {}
}
