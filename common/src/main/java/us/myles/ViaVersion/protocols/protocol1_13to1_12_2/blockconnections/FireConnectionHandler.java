package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FireConnectionHandler extends ConnectionHandler {
    private static final String[] WOOD_TYPES = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static final Map<Byte, Integer> connectedBlocks = new HashMap<>();
    private static final Set<Integer> flammableBlocks = new HashSet<>();

    private static void addWoodTypes(Set<String> set, String suffix) {
        for (String woodType : WOOD_TYPES) {
            set.add("minecraft:" + woodType + suffix);
        }
    }

    static ConnectionData.ConnectorInitAction init() {
        Set<String> flammabeIds = new HashSet<>();
        flammabeIds.add("minecraft:tnt");
        flammabeIds.add("minecraft:vine");
        flammabeIds.add("minecraft:bookshelf");
        flammabeIds.add("minecraft:hay_block");
        flammabeIds.add("minecraft:deadbush");
        addWoodTypes(flammabeIds, "_slab");
        addWoodTypes(flammabeIds, "_log");
        addWoodTypes(flammabeIds, "_planks");
        addWoodTypes(flammabeIds, "_leaves");
        addWoodTypes(flammabeIds, "_fence");
        addWoodTypes(flammabeIds, "_fence_gate");
        addWoodTypes(flammabeIds, "_stairs");

        FireConnectionHandler connectionHandler = new FireConnectionHandler();
        return blockData -> {
            String key = blockData.getMinecraftKey();
            if (key.contains("_wool") || key.contains("_carpet") || flammabeIds.contains(key)) {
                flammableBlocks.add(blockData.getSavedBlockStateId());
            } else if (key.equals("minecraft:fire")) {
                int id = blockData.getSavedBlockStateId();
                connectedBlocks.put(getStates(blockData), id);
                ConnectionData.connectionHandlerMap.put(id, connectionHandler);
            }
        };
    }

    private static byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        if (blockData.getValue("east").equals("true")) states |= 1;
        if (blockData.getValue("north").equals("true")) states |= 2;
        if (blockData.getValue("south").equals("true")) states |= 4;
        if (blockData.getValue("up").equals("true")) states |= 8;
        if (blockData.getValue("west").equals("true")) states |= 16;
        return states;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        byte states = 0;
        if (flammableBlocks.contains(getBlockData(user, position.getRelative(BlockFace.EAST)))) states |= 1;
        if (flammableBlocks.contains(getBlockData(user, position.getRelative(BlockFace.NORTH)))) states |= 2;
        if (flammableBlocks.contains(getBlockData(user, position.getRelative(BlockFace.SOUTH)))) states |= 4;
        if (flammableBlocks.contains(getBlockData(user, position.getRelative(BlockFace.TOP)))) states |= 8;
        if (flammableBlocks.contains(getBlockData(user, position.getRelative(BlockFace.WEST)))) states |= 16;
        return connectedBlocks.get(states);
    }
}
