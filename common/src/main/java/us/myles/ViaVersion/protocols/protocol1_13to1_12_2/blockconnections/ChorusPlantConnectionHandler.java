package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.Map;

public class ChorusPlantConnectionHandler extends AbstractFenceConnectionHandler {

    private Integer endstone;

    static void init() {
        new ChorusPlantConnectionHandler("", "minecraft:chorus_plant");
    }

    public ChorusPlantConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
        endstone = ConnectionData.keyToId.get("minecraft:end_stone");
        for (Map.Entry<String, Integer> entry : ConnectionData.keyToId.entrySet()) {
            if (entry.getKey().split("\\[")[0].equals("minecraft:chorus_flower")) {
                getBlockStates().add(entry.getValue());
            }
        }
    }

    @Override
    protected Byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 16;
        if (blockData.getValue("down").equals("true")) states |= 32;
        return states;
    }

    @Override
    protected Byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (connects(BlockFace.TOP, getBlockData(user, position.getRelative(BlockFace.TOP)))) states |= 16;
        if (connects(BlockFace.BOTTOM, getBlockData(user, position.getRelative(BlockFace.BOTTOM)))) states |= 32;
        return states;
    }

    @Override
    protected boolean connects(BlockFace side, int blockState) {
        return getBlockStates().contains(blockState) || (side == BlockFace.BOTTOM && blockState == endstone);
    }
}
