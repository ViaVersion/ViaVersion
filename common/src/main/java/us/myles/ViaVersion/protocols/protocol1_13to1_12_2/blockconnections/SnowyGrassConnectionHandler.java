package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SnowyGrassConnectionHandler extends ConnectionHandler {
    private static Map<Pair<Integer, Boolean>, Integer> grassBlocks = new HashMap<>();
    private static Set<Integer> snows = new HashSet<>();

    static void init() {
        Set<String> snowyGrassBlocks = new HashSet<>();
        snowyGrassBlocks.add("minecraft:grass_block");
        snowyGrassBlocks.add("minecraft:podzol");
        snowyGrassBlocks.add("minecraft:mycelium");

        SnowyGrassConnectionHandler handler = new SnowyGrassConnectionHandler();
        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            WrappedBlockData data = WrappedBlockData.fromString(blockState.getKey());
            if (snowyGrassBlocks.contains(data.getMinecraftKey())) {
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), handler);
                data.set("snowy", "true");
                grassBlocks.put(new Pair<>(blockState.getValue(), true), data.getBlockStateId());
                data.set("snowy", "false");
                grassBlocks.put(new Pair<>(blockState.getValue(), false), data.getBlockStateId());
            }
            if (data.getMinecraftKey().equals("minecraft:snow") || data.getMinecraftKey().equals("minecraft:snow_block")) {
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), handler);
                snows.add(blockState.getValue());
            }
        }
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        int blockUpId = getBlockData(user, position.getRelative(BlockFace.TOP));
        Integer newId = grassBlocks.get(new Pair<>(blockState, snows.contains(blockUpId)));
        if (newId != null) {
            return newId;
        }
        return blockState;
    }
}