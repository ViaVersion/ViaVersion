package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FlowerConnectionHandler extends ConnectionHandler {
    private static Map<Integer, Integer> flowers = new HashMap<>();

    static ConnectionData.ConnectorInitAction init() {
        final Set<String> baseFlower = new HashSet<>();
        baseFlower.add("minecraft:rose_bush");
        baseFlower.add("minecraft:sunflower");
        baseFlower.add("minecraft:peony");
        baseFlower.add("minecraft:tall_grass");
        baseFlower.add("minecraft:large_fern");
        baseFlower.add("minecraft:lilac");

        final FlowerConnectionHandler handler = new FlowerConnectionHandler();
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                if (baseFlower.contains(blockData.getMinecraftKey())) {
                    ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), handler);
                    if (blockData.getValue("half").equals("lower")) {
                        blockData.set("half", "upper");
                        flowers.put(blockData.getSavedBlockStateId(), blockData.getBlockStateId());
                    }
                }
            }
        };
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        int blockBelowId = getBlockData(user, position.getRelative(BlockFace.BOTTOM));
        if (flowers.containsKey(blockBelowId)) {
            int blockAboveId = getBlockData(user, position.getRelative(BlockFace.TOP));
            if (Via.getConfig().isStemWhenBlockAbove()) {
                if (blockAboveId == 0) {
                    return flowers.get(blockBelowId);
                }
            } else if (!flowers.containsKey(blockAboveId)) {
                return flowers.get(blockBelowId);
            }
        }
        return blockState;
    }
}
