package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class ChorusPlantConnectionHandler extends AbstractFenceConnectionHandler {
    private final int endstone;

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
        ChorusPlantConnectionHandler handler = new ChorusPlantConnectionHandler();
        actions.add(handler.getInitAction("minecraft:chorus_plant"));
        actions.add(handler.getExtraAction());
        return actions;
    }

    public ChorusPlantConnectionHandler() {
        super(null);
        endstone = ConnectionData.getId("minecraft:end_stone");
    }

    public ConnectionData.ConnectorInitAction getExtraAction() {
        return blockData -> {
            if (blockData.getMinecraftKey().equals("minecraft:chorus_flower")) {
                getBlockStates().add(blockData.getSavedBlockStateId());
            }
        };
    }

    @Override
    protected byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 16;
        if (blockData.getValue("down").equals("true")) states |= 32;
        return states;
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (connects(BlockFace.TOP, getBlockData(user, position.getRelative(BlockFace.TOP)), false)) states |= 16;
        if (connects(BlockFace.BOTTOM, getBlockData(user, position.getRelative(BlockFace.BOTTOM)), false)) states |= 32;
        return states;
    }

    @Override
    protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
        return getBlockStates().contains(blockState) || (side == BlockFace.BOTTOM && blockState == endstone);
    }
}
